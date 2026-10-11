package com.ruoyi.assess.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 综测审核记录表 assess_review
 *
 * @author ruoyi
 */
public class AssessReview extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 审核记录ID */
    @Excel(name = "审核记录ID")
    private Long reviewId;

    /** 申报单ID */
    @Excel(name = "申报单ID")
    private Long declarationId;

    /** 审核人用户ID */
    private Long reviewerId;

    /** 审核人姓名 */
    @Excel(name = "审核人")
    private String reviewerName;

    /** 审核轮次 */
    @Excel(name = "审核轮次")
    private Integer reviewRound;

    /** 审核结果(1通过 2驳回 3退回修改) */
    @Excel(name = "审核结果", readConverterExp = "1=通过,2=驳回,3=退回修改")
    private String reviewResult;

    /** 审核评分 */
    @Excel(name = "审核评分")
    private BigDecimal reviewScore;

    /** 审核意见 */
    @Excel(name = "审核意见")
    private String reviewComment;

    /** 审核时间 */
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    public Long getReviewId()
    {
        return reviewId;
    }

    public void setReviewId(Long reviewId)
    {
        this.reviewId = reviewId;
    }

    public Long getDeclarationId()
    {
        return declarationId;
    }

    public void setDeclarationId(Long declarationId)
    {
        this.declarationId = declarationId;
    }

    public Long getReviewerId()
    {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId)
    {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName()
    {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName)
    {
        this.reviewerName = reviewerName;
    }

    public Integer getReviewRound()
    {
        return reviewRound;
    }

    public void setReviewRound(Integer reviewRound)
    {
        this.reviewRound = reviewRound;
    }

    public String getReviewResult()
    {
        return reviewResult;
    }

    public void setReviewResult(String reviewResult)
    {
        this.reviewResult = reviewResult;
    }

    public BigDecimal getReviewScore()
    {
        return reviewScore;
    }

    public void setReviewScore(BigDecimal reviewScore)
    {
        this.reviewScore = reviewScore;
    }

    public String getReviewComment()
    {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment)
    {
        this.reviewComment = reviewComment;
    }

    public Date getReviewTime()
    {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime)
    {
        this.reviewTime = reviewTime;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("reviewId", getReviewId())
            .append("declarationId", getDeclarationId())
            .append("reviewerId", getReviewerId())
            .append("reviewerName", getReviewerName())
            .append("reviewRound", getReviewRound())
            .append("reviewResult", getReviewResult())
            .append("reviewScore", getReviewScore())
            .append("reviewComment", getReviewComment())
            .append("reviewTime", getReviewTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
