package com.ruoyi.assess.ai.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.ruoyi.assess.ai.ClauseMatchService;
import com.ruoyi.assess.ai.dto.MatchRequest;
import com.ruoyi.assess.ai.dto.MatchResponse;
import com.ruoyi.assess.domain.AssessRegulationClause;
import com.ruoyi.common.utils.StringUtils;

/**
 * Mock条款匹配服务实现
 * <p>
 * 匹配逻辑：材料名称/类型/OCR文本 与 条款标题/原文 做关键词匹配。
 * 命中 → suggestedScore = clause.maxScore, confidence = 0.60, matchStatus = 1
 * 未命中 → matchStatus = 2, suggestedScore = null
 * <p>
 * 后续真实盘古实现直接替换此类，不改调用方。
 *
 * @author ruoyi
 */
@ConditionalOnProperty(prefix = "assess.ai", name = "enabled", havingValue = "false", matchIfMissing = true)
@Service
public class MockClauseMatchServiceImpl implements ClauseMatchService
{
    private static final Logger log = LoggerFactory.getLogger(MockClauseMatchServiceImpl.class);

    /** Mock固定置信度 */
    private static final BigDecimal MOCK_CONFIDENCE = new BigDecimal("0.60");

    @Override
    public MatchResponse match(MatchRequest request, List<AssessRegulationClause> candidateClauses)
    {
        MatchResponse response = new MatchResponse();
        response.setMaterialId(request.getMaterialId());

        if (candidateClauses == null || candidateClauses.isEmpty())
        {
            response.setMatchStatus("2");
            response.setMatchReason("无候选条款可供匹配");
            log.info("材料[{}]无候选条款，匹配状态=未匹配", request.getMaterialId());
            return response;
        }

        // 拼接材料关键词：名称 + 类型 + OCR文本
        String materialKeywords = buildMaterialKeywords(request);

        // 遍历候选条款，找第一个关键词命中的
        for (AssessRegulationClause clause : candidateClauses)
        {
            if (isMatch(materialKeywords, clause))
            {
                response.setClauseId(clause.getClauseId());
                response.setSuggestedScore(clause.getMaxScore());
                response.setEvidenceClauseNo(clause.getClauseNo());
                response.setEvidenceClauseText(clause.getClauseText());
                response.setEvidenceConfidence(MOCK_CONFIDENCE);
                response.setMatchStatus("1");
                response.setMatchReason(String.format(
                    "材料「%s」关键词命中条款[%s]%s",
                    request.getMaterialName(), clause.getClauseNo(), clause.getClauseTitle()));
                log.info("材料[{}]命中条款[{}], 建议分={}, 置信度={}",
                    request.getMaterialId(), clause.getClauseNo(), clause.getMaxScore(), MOCK_CONFIDENCE);
                return response;
            }
        }

        // 全部未命中
        response.setMatchStatus("2");
        response.setMatchReason("材料关键词未命中任何候选条款");
        log.info("材料[{}]未命中任何条款，匹配状态=未匹配", request.getMaterialId());
        return response;
    }

    /**
     * 拼接材料关键词文本（小写化便于匹配）
     */
    private String buildMaterialKeywords(MatchRequest request)
    {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(request.getMaterialName()))
        {
            sb.append(request.getMaterialName().toLowerCase());
        }
        if (StringUtils.isNotEmpty(request.getMaterialType()))
        {
            sb.append(" ").append(request.getMaterialType().toLowerCase());
        }
        if (StringUtils.isNotEmpty(request.getOcrText()))
        {
            sb.append(" ").append(request.getOcrText().toLowerCase());
        }
        return sb.toString();
    }

    /**
     * 关键词匹配策略（优先级从高到低）：
     * 1. 精确包含：材料文本完全包含条款标题 → 命中
     * 2. 中文子串匹配：从条款标题提取所有2~4字连续中文子串，任一出现在材料文本中 → 命中
     * 3. 条款原文片段匹配
     */
    private boolean isMatch(String materialKeywords, AssessRegulationClause clause)
    {
        if (StringUtils.isEmpty(materialKeywords))
        {
            return false;
        }

        String clauseTitle = clause.getClauseTitle();
        String clauseText = clause.getClauseText();
        String lowerMaterial = materialKeywords.toLowerCase();

        // 策略1：材料文本包含条款标题（精确匹配）
        if (StringUtils.isNotEmpty(clauseTitle) && clauseTitle.length() >= 2)
        {
            if (lowerMaterial.contains(clauseTitle.toLowerCase()))
            {
                return true;
            }
        }

        // 策略2：中文子串匹配 — 从条款标题提取2~4字连续中文片段在材料文本中搜索
        if (StringUtils.isNotEmpty(clauseTitle))
        {
            List<String> substrings = extractChineseSubstrings(clauseTitle, 2, 4);
            for (String sub : substrings)
            {
                if (lowerMaterial.contains(sub.toLowerCase()))
                {
                    return true;
                }
            }
        }

        // 策略3：材料文本包含条款原文中的关键片段
        if (StringUtils.isNotEmpty(clauseText) && clauseText.length() >= 2)
        {
            String fragment = clauseText.length() > 20
                ? clauseText.substring(0, 20).toLowerCase()
                : clauseText.toLowerCase();
            if (lowerMaterial.contains(fragment))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * 从中文文本中提取所有长度在[minLen, maxLen]之间的连续中文子串
     * 例如 "竞赛类-国家级一等奖" → ["竞赛", "赛类", "国家", "家级", "级一", "一等", "等级",
     *   "竞赛类", "赛类", "国家级", "家级一", "级一等", "一等奖", "国家级一", ...]
     * 只保留有意义的片段（去掉含"类"等无意义后缀的）
     */
    private List<String> extractChineseSubstrings(String text, int minLen, int maxLen)
    {
        List<String> result = new ArrayList<>();
        if (StringUtils.isEmpty(text))
        {
            return result;
        }

        // 提取所有连续中文段
        List<String> chineseSegments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (char c : text.toCharArray())
        {
            if (isChineseChar(c))
            {
                current.append(c);
            }
            else
            {
                if (current.length() > 0)
                {
                    chineseSegments.add(current.toString());
                    current.setLength(0);
                }
            }
        }
        if (current.length() > 0)
        {
            chineseSegments.add(current.toString());
        }

        // 从每个中文段中提取所有长度在[minLen, maxLen]的子串
        for (String segment : chineseSegments)
        {
            for (int len = minLen; len <= maxLen && len <= segment.length(); len++)
            {
                for (int i = 0; i <= segment.length() - len; i++)
                {
                    String sub = segment.substring(i, i + len);
                    // 过滤掉含"类"的片段（如"赛类"、"竞赛类"），因为材料文本通常不含"类"
                    if (!sub.contains("类"))
                    {
                        result.add(sub);
                    }
                }
            }
        }

        return result;
    }

    /**
     * 判断字符是否为中文字符
     */
    private boolean isChineseChar(char c)
    {
        return c >= '\u4e00' && c <= '\u9fff';
    }
}
