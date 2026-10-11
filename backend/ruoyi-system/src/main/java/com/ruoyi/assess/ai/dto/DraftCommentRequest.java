package com.ruoyi.assess.ai.dto;

/**
 * AI驳回意见草稿入参DTO
 * <p>
 * 对应 POST /assess/review/draftComment 接口请求体
 *
 * @author ruoyi
 */
public class DraftCommentRequest
{
    /** 申报单ID */
    private Long declarationId;

    public Long getDeclarationId()
    {
        return declarationId;
    }

    public void setDeclarationId(Long declarationId)
    {
        this.declarationId = declarationId;
    }
}
