package com.ruoyi.assess.ai.impl;

import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.ruoyi.assess.ai.ReviewCommentService;
import com.ruoyi.assess.ai.dto.DraftCommentRequest;
import com.ruoyi.assess.domain.AssessDeclarationItem;
import com.ruoyi.assess.mapper.AssessDeclarationItemMapper;
import com.ruoyi.common.utils.StringUtils;

/**
 * Mock驳回意见草稿服务实现
 * <p>
 * 逻辑：
 * 1. 按declarationId查出所有申报明细
 * 2. 筛选被规则引擎砍分的明细：finalScore < suggestedScore 或 ruleCheckResult 非空
 * 3. 逐条拼接 matchReason + ruleCheckResult 生成自然语言草稿
 * <p>
 * AGENTS.md 第5条合规：只产出"建议"草稿，不产出分值，不直接入库。
 * 后续真实盘古实现直接替换此类，不改调用方。
 *
 * @author ruoyi
 */
@ConditionalOnProperty(prefix = "assess.ai", name = "enabled", havingValue = "false", matchIfMissing = true)
@Service
public class MockReviewCommentServiceImpl implements ReviewCommentService
{
    private static final Logger log = LoggerFactory.getLogger(MockReviewCommentServiceImpl.class);

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

        StringBuilder draft = new StringBuilder();
        int issueCount = 0;

        for (AssessDeclarationItem item : items)
        {
            boolean scoreReduced = isScoreReduced(item);
            boolean hasRuleCheck = StringUtils.isNotEmpty(item.getRuleCheckResult())
                    && !"[]".equals(item.getRuleCheckResult());

            if (scoreReduced || hasRuleCheck)
            {
                issueCount++;
                draft.append(String.format("%d. ", issueCount));

                // 匹配原因
                if (StringUtils.isNotEmpty(item.getMatchReason()))
                {
                    draft.append("【匹配原因】").append(item.getMatchReason());
                }

                // 规则校验结果
                if (hasRuleCheck)
                {
                    if (draft.length() > 0 && draft.charAt(draft.length() - 1) != ' ')
                    {
                        draft.append("；");
                    }
                    draft.append("【规则校验】").append(item.getRuleCheckResult());
                }

                // 分值变化
                if (scoreReduced)
                {
                    draft.append(String.format("（建议分：%s → 最终分：%s）",
                        item.getSuggestedScore(), item.getFinalScore()));
                }

                draft.append("\n");
            }
        }

        if (issueCount == 0)
        {
            return "经审核，该申报单各项明细未发现规则引擎砍分情况，暂无需生成驳回意见。";
        }

        String header = String.format("经审核，该申报单共有 %d 项明细存在以下问题，建议驳回或要求学生修改：\n", issueCount);
        log.info("申报单[{}]生成驳回意见草稿，共{}条问题明细", declarationId, issueCount);
        return header + draft.toString();
    }

    /**
     * 判断明细是否被规则引擎砍分：finalScore < suggestedScore
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
