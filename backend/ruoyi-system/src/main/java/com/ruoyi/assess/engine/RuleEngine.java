package com.ruoyi.assess.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.assess.domain.AssessDeclarationItem;
import com.ruoyi.assess.domain.AssessRuleConfig;
import com.ruoyi.assess.mapper.AssessDeclarationItemMapper;
import com.ruoyi.common.utils.StringUtils;

/**
 * 规则引擎 — 纯Java实现，禁止调大模型
 * <p>
 * 执行顺序：time_window → mutex → cap
 * 先过滤不合规，再择优，最后封顶
 * <p>
 * 兜底铁律：
 * - 时间窗取不到日期 → 记"未校验：缺日期数据"，不调整分值，绝不清零
 * - 所有规则找不到适用条件 → 跳过，final_score = suggested_score
 *
 * @author ruoyi
 */
@Component
public class RuleEngine
{
    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    @Autowired
    private AssessDeclarationItemMapper itemMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 对一张申报单的所有匹配结果执行规则校验
     *
     * @param items 申报明细列表（含AI匹配后的suggestedScore）
     * @param rules 规则配置列表
     * @return 规则引擎结果（含每条明细的finalScore + 总分）
     */
    public RuleEngineResult executeRules(List<AssessDeclarationItem> items,
                                         List<AssessRuleConfig> rules)
    {
        RuleEngineResult result = new RuleEngineResult();
        List<ItemRuleResult> itemResults = new ArrayList<>();

        // 初始化每条明细的校验结果
        for (AssessDeclarationItem item : items)
        {
            ItemRuleResult ir = new ItemRuleResult();
            ir.setItemId(item.getItemId());
            ir.setMaterialId(item.getMaterialId());
            ir.setClauseId(item.getClauseId());
            ir.setClauseNo(item.getClauseNo());
            ir.setClauseType(item.getClauseType());
            // 初始分值 = AI建议分（未匹配的为0）
            ir.setSuggestedScore(item.getSuggestedScore() != null
                ? item.getSuggestedScore() : BigDecimal.ZERO);
            ir.setCheckDetails(new ArrayList<>());
            itemResults.add(ir);
        }

        if (rules == null || rules.isEmpty())
        {
            // 无规则：final_score = suggested_score
            finalizeScores(itemResults);
            result.setItemResults(itemResults);
            result.setTotalScore(calculateTotal(itemResults));
            log.info("规则引擎：无规则配置，final_score = suggested_score");
            return result;
        }

        // ── 1. 时间窗校验（time_window）──
        List<AssessRuleConfig> timeWindowRules = filterByType(rules, "time_window");
        executeTimeWindow(itemResults, timeWindowRules);

        // ── 2. 互斥校验（mutex）──
        List<AssessRuleConfig> mutexRules = filterByType(rules, "mutex");
        executeMutex(itemResults, mutexRules);

        // ── 3. 封顶校验（cap）──
        List<AssessRuleConfig> capRules = filterByType(rules, "cap");
        executeCap(itemResults, capRules);

        // ── 4. 产出最终分 ──
        finalizeScores(itemResults);

        result.setItemResults(itemResults);
        result.setTotalScore(calculateTotal(itemResults));
        log.info("规则引擎执行完成，共{}条明细，总分={}", itemResults.size(), result.getTotalScore());
        return result;
    }

    /**
     * 时间窗校验
     * 兜底：取不到日期 → 记"未校验：缺日期数据"，不调整分值
     */
    private void executeTimeWindow(List<ItemRuleResult> itemResults, List<AssessRuleConfig> rules)
    {
        for (AssessRuleConfig rule : rules)
        {
            JsonNode condition = parseJson(rule.getRuleCondition());
            if (condition == null)
            {
                continue;
            }

            String dateField = condition.has("date_field") ? condition.get("date_field").asText() : null;
            String startStr = condition.has("start") ? condition.get("start").asText() : null;
            String endStr = condition.has("end") ? condition.get("end").asText() : null;

            if (StringUtils.isEmpty(dateField) || StringUtils.isEmpty(startStr) || StringUtils.isEmpty(endStr))
            {
                log.warn("时间窗规则[{}]配置不完整，跳过", rule.getRuleName());
                continue;
            }

            LocalDate startDate = parseLocalDate(startStr);
            LocalDate endDate = parseLocalDate(endStr);
            if (startDate == null || endDate == null)
            {
                log.warn("时间窗规则[{}]日期格式错误，跳过", rule.getRuleName());
                continue;
            }

            for (ItemRuleResult ir : itemResults)
            {
                if (ir.getSuggestedScore().compareTo(BigDecimal.ZERO) == 0)
                {
                    continue;
                }

                // 从材料结构化字段取日期
                String dateValue = null;
                if (ir.getMaterialId() != null && ir.getMaterialId() > 0)
                {
                    dateValue = itemMapper.selectMaterialFieldValue(ir.getMaterialId(), dateField);
                }

                if (StringUtils.isEmpty(dateValue))
                {
                    // 兜底：缺日期数据，不调整分值，绝不清零
                    ir.addCheckDetail("time_window", true,
                        String.format("未校验：缺日期数据(字段%s)", dateField));
                    continue;
                }

                LocalDate awardDate = parseLocalDate(dateValue);
                if (awardDate == null)
                {
                    ir.addCheckDetail("time_window", true,
                        String.format("未校验：日期格式无法解析(%s)", dateValue));
                    continue;
                }

                if (awardDate.isBefore(startDate) || awardDate.isAfter(endDate))
                {
                    ir.setSuggestedScore(BigDecimal.ZERO);
                    ir.addCheckDetail("time_window", false,
                        String.format("材料日期%s不在有效区间%s~%s内，该项不加分",
                            dateValue, startStr, endStr));
                }
                else
                {
                    ir.addCheckDetail("time_window", true,
                        String.format("材料日期%s在有效区间%s~%s内", dateValue, startStr, endStr));
                }
            }
        }
    }

    /**
     * 互斥校验：一组互斥条款中，同一材料只能取一条，择优保留最高分
     */
    private void executeMutex(List<ItemRuleResult> itemResults, List<AssessRuleConfig> rules)
    {
        for (AssessRuleConfig rule : rules)
        {
            JsonNode condition = parseJson(rule.getRuleCondition());
            if (condition == null || !condition.has("clause_ids"))
            {
                continue;
            }

            // 解析互斥条款ID列表
            List<Long> clauseIds = new ArrayList<>();
            for (JsonNode node : condition.get("clause_ids"))
            {
                clauseIds.add(node.asLong());
            }
            if (clauseIds.isEmpty())
            {
                continue;
            }

            // 找到命中互斥条款组的明细
            List<ItemRuleResult> hitItems = itemResults.stream()
                .filter(ir -> ir.getClauseId() != null && ir.getClauseId() > 0
                    && clauseIds.contains(ir.getClauseId()))
                .collect(Collectors.toList());

            if (hitItems.size() <= 1)
            {
                continue;
            }

            // 按建议分降序，取最高分项
            hitItems.sort((a, b) -> b.getSuggestedScore().compareTo(a.getSuggestedScore()));
            ItemRuleResult winner = hitItems.get(0);

            for (int i = 1; i < hitItems.size(); i++)
            {
                ItemRuleResult loser = hitItems.get(i);
                loser.setSuggestedScore(BigDecimal.ZERO);
                loser.addCheckDetail("mutex", false,
                    String.format("与条款%s互斥，已择优保留条款%s(建议分%s)，本项不重复加分",
                        winner.getClauseNo(), winner.getClauseNo(), winner.getSuggestedScore()));
            }
            winner.addCheckDetail("mutex", true,
                String.format("互斥组内择优保留(共%d条候选，取最高分%s)",
                    hitItems.size(), winner.getSuggestedScore()));
        }
    }

    /**
     * 封顶校验：同一类别总分不超过上限，超出从最低分项扣减
     */
    private void executeCap(List<ItemRuleResult> itemResults, List<AssessRuleConfig> rules)
    {
        for (AssessRuleConfig rule : rules)
        {
            JsonNode condition = parseJson(rule.getRuleCondition());
            JsonNode action = parseJson(rule.getRuleAction());
            if (condition == null || action == null)
            {
                continue;
            }

            String clauseType = condition.has("clause_type") ? condition.get("clause_type").asText() : null;
            if (StringUtils.isEmpty(clauseType) || !action.has("max_total"))
            {
                continue;
            }

            BigDecimal maxTotal = action.get("max_total").decimalValue();

            // 按条款类型聚合
            List<ItemRuleResult> categoryItems = itemResults.stream()
                .filter(ir -> clauseType.equals(ir.getClauseType()))
                .collect(Collectors.toList());

            if (categoryItems.isEmpty())
            {
                continue;
            }

            BigDecimal categoryTotal = categoryItems.stream()
                .map(ItemRuleResult::getSuggestedScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (categoryTotal.compareTo(maxTotal) > 0)
            {
                // 从最低分项开始扣减
                categoryItems.sort(Comparator.comparing(ItemRuleResult::getSuggestedScore));
                BigDecimal excess = categoryTotal.subtract(maxTotal);
                for (ItemRuleResult ir : categoryItems)
                {
                    if (excess.compareTo(BigDecimal.ZERO) <= 0)
                    {
                        break;
                    }
                    BigDecimal deduct = ir.getSuggestedScore().min(excess);
                    ir.setSuggestedScore(ir.getSuggestedScore().subtract(deduct));
                    excess = excess.subtract(deduct);
                    ir.addCheckDetail("cap", false,
                        String.format("类别[%s]总分%s超过封顶%s，本项扣减%s",
                            clauseType, categoryTotal, maxTotal, deduct));
                }
            }
        }
    }

    /**
     * 产出最终分：rule_adjusted_score = 校验后的suggestedScore, final_score = rule_adjusted_score
     */
    private void finalizeScores(List<ItemRuleResult> itemResults)
    {
        for (ItemRuleResult ir : itemResults)
        {
            ir.setRuleAdjustedScore(ir.getSuggestedScore());
            ir.setFinalScore(ir.getRuleAdjustedScore());
        }
    }

    /**
     * 计算总分
     */
    private BigDecimal calculateTotal(List<ItemRuleResult> itemResults)
    {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemRuleResult ir : itemResults)
        {
            if (ir.getFinalScore() != null)
            {
                total = total.add(ir.getFinalScore());
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    // ========== 工具方法 ==========

    private List<AssessRuleConfig> filterByType(List<AssessRuleConfig> rules, String type)
    {
        return rules.stream()
            .filter(r -> type.equals(r.getRuleType()))
            .collect(Collectors.toList());
    }

    private JsonNode parseJson(String json)
    {
        if (StringUtils.isEmpty(json))
        {
            return null;
        }
        try
        {
            return objectMapper.readTree(json);
        }
        catch (Exception e)
        {
            log.warn("JSON解析失败: {}", json, e);
            return null;
        }
    }

    private LocalDate parseLocalDate(String dateStr)
    {
        if (StringUtils.isEmpty(dateStr))
        {
            return null;
        }
        // 尝试 yyyy-MM-dd
        try
        {
            return LocalDate.parse(dateStr);
        }
        catch (Exception e)
        {
            // 尝试 yyyy/MM/dd
            try
            {
                return LocalDate.parse(dateStr.replace("/", "-"));
            }
            catch (Exception e2)
            {
                return null;
            }
        }
    }
}
