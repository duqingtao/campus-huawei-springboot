package com.ruoyi.assess.domain;

import java.math.BigDecimal;

/**
 * 班级统计概览 DTO
 * <p>
 * 不映射数据库表，仅用于概览接口返回
 *
 * @author ruoyi
 */
public class AssessStatsOverview
{
    /** 学生数 */
    private Integer studentCount;

    /** 待审申报单数(status=1,已提交未审) */
    private Integer pendingCount;

    /** 审核中申报单数(status=2,预留) */
    private Integer reviewingCount;

    /** 通过申报单数(status=3部分通过/4全部通过) */
    private Integer passedCount;

    /** 驳回申报单数(status=5) */
    private Integer rejectedCount;

    /** 平均分(排除草稿) */
    private BigDecimal avgScore;

    public Integer getStudentCount()
    {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount)
    {
        this.studentCount = studentCount;
    }

    public Integer getPendingCount()
    {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount)
    {
        this.pendingCount = pendingCount;
    }

    public Integer getReviewingCount()
    {
        return reviewingCount;
    }

    public void setReviewingCount(Integer reviewingCount)
    {
        this.reviewingCount = reviewingCount;
    }

    public Integer getPassedCount()
    {
        return passedCount;
    }

    public void setPassedCount(Integer passedCount)
    {
        this.passedCount = passedCount;
    }

    public Integer getRejectedCount()
    {
        return rejectedCount;
    }

    public void setRejectedCount(Integer rejectedCount)
    {
        this.rejectedCount = rejectedCount;
    }

    public BigDecimal getAvgScore()
    {
        return avgScore;
    }

    public void setAvgScore(BigDecimal avgScore)
    {
        this.avgScore = avgScore;
    }
}
