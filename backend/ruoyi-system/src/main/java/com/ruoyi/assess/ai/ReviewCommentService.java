package com.ruoyi.assess.ai;

import com.ruoyi.assess.ai.dto.DraftCommentRequest;

/**
 * AI驳回意见草稿服务接口
 * <p>
 * 统一定义在 com.ruoyi.assess.ai 包，禁止散落在业务代码中。
 * 当前使用 MockReviewCommentServiceImpl，后续真实盘古实现直接替换Mock，不改调用方。
 * <p>
 * AGENTS.md 第5条合规：只产出"建议"草稿，不产出分值，不直接入库。
 *
 * @author ruoyi
 */
public interface ReviewCommentService
{
    /**
     * 根据申报单中被规则引擎砍分的明细，生成驳回意见草稿
     *
     * @param request 入参(declarationId)
     * @return 自然语言驳回理由草稿文本
     */
    String generateDraftComment(DraftCommentRequest request);
}
