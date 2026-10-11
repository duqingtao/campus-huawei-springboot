package com.ruoyi.assess.ai.impl;

import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.assess.ai.ReviewCommentService;
import com.ruoyi.assess.ai.client.JsonResponseUtil;
import com.ruoyi.assess.ai.client.MaaSClient;
import com.ruoyi.assess.ai.config.AiConfig;
import com.ruoyi.assess.ai.dto.DraftCommentRequest;
import com.ruoyi.assess.domain.AssessDeclarationItem;
import com.ruoyi.assess.mapper.AssessDeclarationItemMapper;
import com.ruoyi.common.utils.StringUtils;

/**
 * MaaS驳回意见草稿服务实现(Step C)
 * <p>
 * 调用大模型生成驳回意见草稿，只产出"建议"文本，不产出分值。
 * 降级：调用失败时回退到规则拼接方式生成草稿。
 * <p>
 * AGENTS.md第5条合规：只产出"建议"草稿，不直接入库。
 *
 * @author ruoyi
 */
@Service
@ConditionalOnProperty(prefix = "assess.ai", name = "enabled", havingValue = "true")
public class MaaSReviewCommentServiceImpl implements ReviewCommentService
{
    private static final Logger log = LoggerFactory.getLogger(MaaSReviewCommentServiceImpl.class);

    /** 系统提示词 */
    private static final String SYSTEM_PROMPT =
        "你是综测审核助手。根据以下申报明细和审核上下文，起草一段驳回意见。" +
        "只输出JSON，不要输出其他内容。JSON格式如下：\n" +
        "{\"draft_comment\": \"驳回意见草稿，不超过200字\"}";

    @Autowired
    private MaaSClient maaSClient;

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private AssessDeclarationItemMapper itemMapper;

    @Override
    public String generateDraftComment(DraftCommentRequest request)
    {
        Long declarationId = request.getDeclarationId();
        if (declarationId == null)
        {
            return "申报单ID为空，无法生成草稿。";
        }

        List<AssessDeclarationItem> items = itemMapper.selectItemByDeclarationId(declarationId);
        if (items == null || items.isEmpty())
        {
            return "该申报单没有明细，无需生成驳回意见。";
        }

        // 构建上下文
        StringBuilder userContent = new StringBuilder();
        userContent.append("申报单ID：").append(declarationId).append("\n");
        userContent.append("存在问题的明细：\n");

        int issueCount = 0;
        for (AssessDeclarationItem item : items)
        {
            boolean scoreReduced = isScoreReduced(item);
            boolean hasRuleCheck = StringUtils.isNotEmpty(item.getRuleCheckResult())
                    && !"[]".equals(item.getRuleCheckResult());

            if (scoreReduced || hasRuleCheck)
            {
                issueCount++;
                userContent.append(String.format("  %d. 材料名称=%s, 匹配条款=%s, 建议分=%s, 最终分=%s",
                        issueCount, item.getMaterialName(), item.getEvidenceClauseNo(),
                        item.getSuggestedScore(), item.getFinalScore()));
                if (StringUtils.isNotEmpty(item.getMatchReason()))
                {
                    userContent.append(", 匹配原因=").append(item.getMatchReason());
                }
                if (hasRuleCheck)
                {
                    userContent.append(", 规则校验=").append(item.getRuleCheckResult());
                }
                userContent.append("\n");
            }
        }

        if (issueCount == 0)
        {
            return "经审核，该申报单各项明细未发现规则引擎砍分情况，暂无需生成驳回意见。";
        }

        // 调用大模型
        String response = maaSClient.chatCompletion(SYSTEM_PROMPT, userContent.toString(), aiConfig.getModel());
        if (StringUtils.isEmpty(response))
        {
            log.warn("驳回意见草稿：大模型返回空响应，回退到规则拼接");
            return fallbackDraft(declarationId, issueCount, items);
        }

        // 解析JSON
        JSONObject json = JsonResponseUtil.parseJsonObject(response);
        if (json == null)
        {
            log.warn("驳回意见草稿：JSON解析失败，回退到规则拼接, raw={}", response);
            return fallbackDraft(declarationId, issueCount, items);
        }

        String draftComment = JsonResponseUtil.getString(json, "draft_comment");
        if (StringUtils.isEmpty(draftComment))
        {
            log.warn("驳回意见草稿：draft_comment字段为空，回退到规则拼接");
            return fallbackDraft(declarationId, issueCount, items);
        }

        log.info("申报单[{}]生成AI驳回意见草稿，共{}条问题明细", declarationId, issueCount);
        return draftComment;
    }

    /**
     * 降级：规则拼接方式生成草稿(与Mock逻辑一致)
     */
    private String fallbackDraft(Long declarationId, int issueCount, List<AssessDeclarationItem> items)
    {
        StringBuilder draft = new StringBuilder();
        int idx = 0;
        for (AssessDeclarationItem item : items)
        {
            boolean scoreReduced = isScoreReduced(item);
            boolean hasRuleCheck = StringUtils.isNotEmpty(item.getRuleCheckResult())
                    && !"[]".equals(item.getRuleCheckResult());

            if (scoreReduced || hasRuleCheck)
            {
                idx++;
                draft.append(idx).append(". ");
                if (StringUtils.isNotEmpty(item.getMatchReason()))
                {
                    draft.append("【匹配原因】").append(item.getMatchReason());
                }
                if (hasRuleCheck)
                {
                    if (draft.length() > 0 && draft.charAt(draft.length() - 1) != ' ')
                    {
                        draft.append("；");
                    }
                    draft.append("【规则校验】").append(item.getRuleCheckResult());
                }
                if (scoreReduced)
                {
                    draft.append(String.format("（建议分：%s → 最终分：%s）",
                        item.getSuggestedScore(), item.getFinalScore()));
                }
                draft.append("\n");
            }
        }
        return String.format("经审核，该申报单共有 %d 项明细存在以下问题，建议驳回或要求学生修改：\n", issueCount)
                + draft.toString();
    }

    /**
     * 判断明细是否被规则引擎砍分
     */
    private boolean isScoreReduced(AssessDeclarationItem item)
    {
        BigDecimal suggested = item.getSuggestedScore();
        BigDecimal finalScore = item.getFinalScore();
        if (suggested == null || finalScore == null)
        {
            return false;
        }
        return finalScore.compareTo(suggested) < 0;
    }
}
