package com.ruoyi.assess.ai.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.assess.ai.ClauseMatchService;
import com.ruoyi.assess.ai.client.JsonResponseUtil;
import com.ruoyi.assess.ai.client.MaaSClient;
import com.ruoyi.assess.ai.config.AiConfig;
import com.ruoyi.assess.ai.dto.MatchRequest;
import com.ruoyi.assess.ai.dto.MatchResponse;
import com.ruoyi.assess.domain.AssessRegulationClause;
import com.ruoyi.common.utils.StringUtils;

/**
 * MaaS条款匹配服务实现(Step 2)
 * <p>
 * 调用大模型进行语义匹配，产出建议分+证据+置信度。
 * 条款数量多时分批匹配(每批≤10条)，结果合并取最高分。
 * suggested_score超过max_score时截断到max_score并标注。
 * 无匹配条款时clause_id=null，不强行配对。
 * <p>
 * AGENTS.md第5条合规：suggested_score仅供参考，不直接入库为final_score。
 * 降级：调用失败时返回未匹配状态，不抛异常。
 *
 * @author ruoyi
 */
@Service
@ConditionalOnProperty(prefix = "assess.ai", name = "enabled", havingValue = "true")
public class MaaSClauseMatchServiceImpl implements ClauseMatchService
{
    private static final Logger log = LoggerFactory.getLogger(MaaSClauseMatchServiceImpl.class);

    /** 每批最多10条条款 */
    private static final int BATCH_SIZE = 10;

    /** 置信度范围0-1 */
    private static final BigDecimal CONFIDENCE_MIN = BigDecimal.ZERO;
    private static final BigDecimal CONFIDENCE_MAX = BigDecimal.ONE;

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private MaaSClient maaSClient;

    @Override
    public MatchResponse match(MatchRequest request, List<AssessRegulationClause> candidateClauses)
    {
        MatchResponse response = new MatchResponse();
        response.setMaterialId(request.getMaterialId());

        if (candidateClauses == null || candidateClauses.isEmpty())
        {
            response.setMatchStatus("2");
            response.setMatchReason("无候选条款可供匹配");
            return response;
        }

        // 分批匹配
        List<MatchResponse> batchResults = new ArrayList<>();
        int totalClauses = candidateClauses.size();
        for (int i = 0; i < totalClauses; i += BATCH_SIZE)
        {
            int end = Math.min(i + BATCH_SIZE, totalClauses);
            List<AssessRegulationClause> batch = candidateClauses.subList(i, end);
            MatchResponse batchResult = matchBatch(request, batch);
            if (batchResult != null && "1".equals(batchResult.getMatchStatus()))
            {
                batchResults.add(batchResult);
            }
        }

        // 合并取最高分
        if (batchResults.isEmpty())
        {
            response.setMatchStatus("2");
            response.setMatchReason("材料未匹配到任何候选条款");
            log.info("材料[{}]未匹配到任何条款", request.getMaterialId());
            return response;
        }

        MatchResponse best = batchResults.get(0);
        for (MatchResponse r : batchResults)
        {
            if (r.getSuggestedScore() != null && best.getSuggestedScore() != null
                    && r.getSuggestedScore().compareTo(best.getSuggestedScore()) > 0)
            {
                best = r;
            }
        }

        response.setClauseId(best.getClauseId());
        response.setSuggestedScore(best.getSuggestedScore());
        response.setEvidenceClauseNo(best.getEvidenceClauseNo());
        response.setEvidenceClauseText(best.getEvidenceClauseText());
        response.setEvidenceConfidence(best.getEvidenceConfidence());
        response.setMatchStatus("1");
        response.setMatchReason(best.getMatchReason());
        log.info("材料[{}]匹配条款[{}], 建议分={}, 置信度={}",
                request.getMaterialId(), best.getEvidenceClauseNo(),
                best.getSuggestedScore(), best.getEvidenceConfidence());
        return response;
    }

    /**
     * 单批次匹配(≤10条条款)
     */
    private MatchResponse matchBatch(MatchRequest request, List<AssessRegulationClause> batch)
    {
        String systemPrompt =
            "你是综测加分认定专家。给定一份证明材料的信息和若干候选细则条款，" +
            "请判断材料与哪条条款最匹配。只输出JSON，不要输出其他内容。JSON格式如下：\n" +
            "{\n" +
            "  \"clause_id\": 被匹配条款的ID(整数),\n" +
            "  \"suggested_score\": 建议分值(不超过该条款max_score),\n" +
            "  \"evidence_clause_no\": \"条款编号\",\n" +
            "  \"evidence_confidence\": 匹配置信度(0到1之间),\n" +
            "  \"match_reason\": \"一句话说明匹配理由\"\n" +
            "}\n" +
            "如果没有匹配的条款，输出：{\"clause_id\": null, \"match_reason\": \"无匹配\"}";

        // 构建用户输入
        StringBuilder userContent = new StringBuilder();
        userContent.append("材料信息：\n");
        userContent.append("- 材料名称：").append(request.getMaterialName()).append("\n");
        userContent.append("- 材料类型：").append(request.getMaterialType()).append("\n");
        if (StringUtils.isNotEmpty(request.getOcrText()))
        {
            // OCR文本可能很长，截取前2000字符控制token
            String ocrText = request.getOcrText();
            if (ocrText.length() > 2000)
            {
                ocrText = ocrText.substring(0, 2000) + "...(截断)";
            }
            userContent.append("- OCR文本：").append(ocrText).append("\n");
        }
        userContent.append("\n候选条款：\n");
        for (AssessRegulationClause clause : batch)
        {
            userContent.append(String.format(
                "  条款ID=%d, 编号=%s, 标题=%s, 原文=%s, 最高分=%s\n",
                clause.getClauseId(), clause.getClauseNo(),
                clause.getClauseTitle(), clause.getClauseText(),
                clause.getMaxScore()));
        }

        // 调用大模型
        String response = maaSClient.chatCompletion(systemPrompt, userContent.toString(), aiConfig.getModel());
        if (StringUtils.isEmpty(response))
        {
            log.warn("材料[{}]批次匹配：大模型返回空响应", request.getMaterialId());
            return null;
        }

        // 解析JSON
        JSONObject json = JsonResponseUtil.parseJsonObject(response);
        if (json == null)
        {
            log.warn("材料[{}]批次匹配：JSON解析失败, raw={}", request.getMaterialId(), response);
            return null;
        }

        // 提取clause_id
        Long clauseId = JsonResponseUtil.getLong(json, "clause_id");
        if (clauseId == null || clauseId == 0)
        {
            // 无匹配
            return null;
        }

        // 找到对应条款(用于max_score校验)
        AssessRegulationClause matchedClause = null;
        for (AssessRegulationClause clause : batch)
        {
            if (clause.getClauseId() != null && clause.getClauseId().longValue() == clauseId.longValue())
            {
                matchedClause = clause;
                break;
            }
        }

        MatchResponse result = new MatchResponse();
        result.setMaterialId(request.getMaterialId());
        result.setClauseId(clauseId);

        // suggested_score校验：不超过max_score
        BigDecimal maxScore = matchedClause != null ? matchedClause.getMaxScore() : null;
        BigDecimal suggestedScore = JsonResponseUtil.getBigDecimal(json, "suggested_score", BigDecimal.ZERO, maxScore);
        if (suggestedScore != null && maxScore != null && suggestedScore.compareTo(maxScore) > 0)
        {
            suggestedScore = maxScore;
        }
        result.setSuggestedScore(suggestedScore);

        // evidence
        String evidenceClauseNo = JsonResponseUtil.getString(json, "evidence_clause_no");
        if (StringUtils.isEmpty(evidenceClauseNo) && matchedClause != null)
        {
            evidenceClauseNo = matchedClause.getClauseNo();
        }
        result.setEvidenceClauseNo(evidenceClauseNo);

        if (matchedClause != null)
        {
            // 证据链三要素之一：条款原文。clause_text为空时用clauseTitle兜底
            String clauseText = matchedClause.getClauseText();
            if (StringUtils.isEmpty(clauseText))
            {
                clauseText = matchedClause.getClauseTitle();
            }
            result.setEvidenceClauseText(clauseText);
        }
        else
        {
            result.setEvidenceClauseText(JsonResponseUtil.getString(json, "evidence_clause_no"));
        }

        // confidence校验：0-1范围
        BigDecimal confidence = JsonResponseUtil.getBigDecimal(json, "evidence_confidence",
                CONFIDENCE_MIN, CONFIDENCE_MAX);
        result.setEvidenceConfidence(confidence);

        // match_reason
        String matchReason = JsonResponseUtil.getString(json, "match_reason");
        if (suggestedScore != null && maxScore != null && suggestedScore.compareTo(maxScore) == 0)
        {
            // 如果截断了，标注
            BigDecimal originalScore = JsonResponseUtil.getBigDecimal(json, "suggested_score", null, null);
            if (originalScore != null && originalScore.compareTo(maxScore) > 0)
            {
                matchReason = (matchReason != null ? matchReason + " " : "")
                        + "[注：建议分已从" + originalScore + "截断到max_score=" + maxScore + "]";
            }
        }
        result.setMatchReason(matchReason);
        result.setMatchStatus("1");

        return result;
    }
}
