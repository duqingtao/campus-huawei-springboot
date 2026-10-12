package com.ruoyi.assess.ai.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.assess.ai.ClauseExtractService;
import com.ruoyi.assess.ai.client.MaaSClient;
import com.ruoyi.assess.ai.config.AiConfig;
import com.ruoyi.assess.domain.AssessRegulationClause;
import com.ruoyi.common.utils.StringUtils;

/**
 * MaaS条款抽取服务实现
 * <p>
 * 调用大模型从综测细则文本中提取结构化条款，返回JSON数组。
 * 每条包含: clauseNo(条款编号)、clauseTitle(条款标题)、clauseText(条款原文)、
 * maxScore(分值)、clauseType(类型: 1加分/2减分/3基础/4一票否决)。
 * <p>
 * 降级: ai.enabled=false时返回空列表。
 * JSON解析兜底: 大模型返回可能带```json```包裹，用正则提取[...]段再解析。
 * 逐条解析失败跳过，不中断整体。
 *
 * @author ruoyi
 */
@Service
public class MaaSClauseExtractServiceImpl implements ClauseExtractService
{
    private static final Logger log = LoggerFactory.getLogger(MaaSClauseExtractServiceImpl.class);

    /** 文本截取上限，控制token消耗 */
    private static final int MAX_TEXT_LENGTH = 8000;

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private MaaSClient maaSClient;

    /** 系统提示词 */
    private static final String SYSTEM_PROMPT = "你是综测细则条款提取助手。"
            + "请从以下综测细则文本中提取所有条款，返回JSON数组，每条包含："
            + "clauseNo（条款编号）、clauseTitle（条款标题）、clauseText（条款原文）、"
            + "maxScore（分值，数字）、clauseType（类型：1加分/2减分/3基础/4一票否决）。"
            + "只返回JSON数组，不要添加任何解释说明文字。";

    @Override
    public List<AssessRegulationClause> extractClauses(String text)
    {
        if (StringUtils.isEmpty(text))
        {
            log.warn("条款抽取入参文本为空");
            return new ArrayList<>();
        }

        if (!aiConfig.isEnabled())
        {
            log.warn("AI功能未启用，无法提取条款");
            return new ArrayList<>();
        }

        // 截取前MAX_TEXT_LENGTH字符控制token
        String truncatedText = text.length() > MAX_TEXT_LENGTH
                ? text.substring(0, MAX_TEXT_LENGTH) : text;

        // 调用大模型
        String model = aiConfig.getModel();
        String response = maaSClient.chatCompletion(SYSTEM_PROMPT, truncatedText, model);
        if (StringUtils.isEmpty(response))
        {
            log.error("MaaS条款抽取返回空响应");
            return new ArrayList<>();
        }

        // 解析JSON数组
        return parseClauseJsonArray(response);
    }

    /**
     * 解析大模型返回的条款JSON数组
     * <p>
     * 兜底处理: 大模型可能返回```json [...] ```或前后带多余文字，
     * 用正则提取第一个[到最后一个]之间的内容再解析。
     * 逐条解析失败跳过并计数。
     *
     * @param response 大模型原始返回
     * @return 解析出的条款列表
     */
    private List<AssessRegulationClause> parseClauseJsonArray(String response)
    {
        List<AssessRegulationClause> result = new ArrayList<>();

        // 提取JSON数组段
        String jsonStr = extractJsonArray(response);
        if (StringUtils.isEmpty(jsonStr))
        {
            log.error("无法从大模型响应中提取JSON数组, response={}", response);
            return result;
        }

        JSONArray jsonArray;
        try
        {
            jsonArray = JSON.parseArray(jsonStr);
        }
        catch (Exception e)
        {
            log.error("解析条款JSON数组失败, jsonStr={}", jsonStr, e);
            return result;
        }

        if (jsonArray == null || jsonArray.isEmpty())
        {
            log.warn("条款JSON数组为空");
            return result;
        }

        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < jsonArray.size(); i++)
        {
            try
            {
                JSONObject obj = jsonArray.getJSONObject(i);
                AssessRegulationClause clause = new AssessRegulationClause();

                // clauseNo - 条款编号
                clause.setClauseNo(obj.getString("clauseNo"));

                // clauseTitle - 条款标题
                clause.setClauseTitle(obj.getString("clauseTitle"));

                // clauseText - 条款原文
                clause.setClauseText(obj.getString("clauseText"));

                // maxScore - 分值，容错处理
                clause.setMaxScore(parseMaxScore(obj));

                // clauseType - 类型，默认"1"加分项
                String clauseType = obj.getString("clauseType");
                if (StringUtils.isEmpty(clauseType))
                {
                    clauseType = "1";
                }
                clause.setClauseType(clauseType);

                // 至少要有条款标题才算有效
                if (StringUtils.isNotEmpty(clause.getClauseTitle()))
                {
                    result.add(clause);
                    successCount++;
                }
                else
                {
                    failCount++;
                    log.warn("第{}条条款缺少标题，跳过: {}", i + 1, obj);
                }
            }
            catch (Exception e)
            {
                failCount++;
                log.warn("解析第{}条条款失败: {}", i + 1, e.getMessage());
            }
        }

        log.info("条款抽取完成: 成功{}条, 失败{}条", successCount, failCount);
        return result;
    }

    /**
     * 从大模型响应中提取JSON数组字符串
     * <p>
     * 处理```json [...] ```包裹或前后多余文字的情况。
     */
    private String extractJsonArray(String response)
    {
        String trimmed = response.trim();

        // 去除```json ... ```包裹
        if (trimmed.startsWith("```"))
        {
            // 去掉开头的```json或```
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0)
            {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            // 去掉结尾的```
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence >= 0)
            {
                trimmed = trimmed.substring(0, lastFence);
            }
            trimmed = trimmed.trim();
        }

        // 提取第一个[到最后一个]之间内容
        int start = trimmed.indexOf('[');
        int end = trimmed.lastIndexOf(']');
        if (start >= 0 && end > start)
        {
            return trimmed.substring(start, end + 1);
        }

        return null;
    }

    /**
     * 解析分值，容错处理多种格式
     */
    private BigDecimal parseMaxScore(JSONObject obj)
    {
        try
        {
            // 优先用getBigDecimal
            BigDecimal score = obj.getBigDecimal("maxScore");
            if (score != null)
            {
                return score;
            }

            // 兜底: 当字符串解析
            String scoreStr = obj.getString("maxScore");
            if (StringUtils.isNotEmpty(scoreStr))
            {
                return new BigDecimal(scoreStr.trim());
            }
        }
        catch (Exception e)
        {
            log.warn("解析maxScore失败: {}", obj.get("maxScore"));
        }
        return BigDecimal.ZERO;
    }
}
