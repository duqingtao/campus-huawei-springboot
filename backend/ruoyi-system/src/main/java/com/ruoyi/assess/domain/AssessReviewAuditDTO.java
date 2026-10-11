package com.ruoyi.assess.domain;

/**
 * 审核操作入参 DTO
 * <p>
 * 对应 POST /assess/review/audit 接口请求体
 *
 * @author ruoyi
 */
public class AssessReviewAuditDTO
{
    /** 申报单ID */
    private Long declarationId;

    /** 审核结果(3部分通过 4全部通过 5已驳回) — 即申报单目标status */
    private String result;

    /** 审核颜色(1绿通过 2黄存疑 3红驳回) */
    private String reviewColor;

    /** 审核意见 */
    private String reviewComment;

    public Long getDeclarationId()
    {
        return declarationId;
    }

    public void setDeclarationId(Long declarationId)
    {
        this.declarationId = declarationId;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public String getReviewColor()
    {
        return reviewColor;
    }

    public void setReviewColor(String reviewColor)
    {
        this.reviewColor = reviewColor;
    }

    public String getReviewComment()
    {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment)
    {
        this.reviewComment = reviewComment;
    }
}
