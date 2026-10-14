package com.ruoyi.assess.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 综测申报单主表 assess_declaration
 *
 * @author ruoyi
 */
public class AssessDeclaration extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 申报单ID */
    @Excel(name = "申报单ID")
    private Long declarationId;

    /** 申报单号 */
    @Excel(name = "申报单号")
    private String declarationNo;

    /** 依据细则ID */
    @Excel(name = "细则ID")
    private Long regulationId;

    /** 申报学生用户ID */
    private Long userId;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String studentName;

    /** 班级名称 */
    @Excel(name = "班级名称")
    private String className;

    /** 学年(如2025-2026) */
    @Excel(name = "学年")
    private String academicYear;

    /** 学期(1上学期 2下学期) */
    @Excel(name = "学期", readConverterExp = "1=上学期,2=下学期")
    private String semester;

    /** 总分 */
    @Excel(name = "总分")
    private BigDecimal totalScore;

    /** 状态(0草稿 1已提交 2审核中 3部分通过 4全部通过 5已驳回) */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=已提交,2=审核中,3=部分通过,4=全部通过,5=已驳回")
    private String status;

    /** 提交时间 */
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 审核人用户ID */
    private Long reviewUserId;

    /** 审核时间 */
    private Date reviewTime;

    /** 审核颜色(1绿2黄3红) */
    private String reviewColor;

    /** 审核意见 */
    private String reviewComment;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    // ========== 以下为 transient 查询/操作辅助字段 ==========

    /** 数据隔离：部门ID(班委按dept_id过滤) */
    private Long deptId;

    /** 创建申报单时传入：关联材料ID列表 */
    private List<Long> materialIds;

    /** 查询详情时返回：申报明细列表 */
    private List<AssessDeclarationItem> items;

    /** 细则标题(关联查询) */
    private String regulationTitle;

    public Long getDeclarationId()
    {
        return declarationId;
    }

    public void setDeclarationId(Long declarationId)
    {
        this.declarationId = declarationId;
    }

    public String getDeclarationNo()
    {
        return declarationNo;
    }

    public void setDeclarationNo(String declarationNo)
    {
        this.declarationNo = declarationNo;
    }

    public Long getRegulationId()
    {
        return regulationId;
    }

    public void setRegulationId(Long regulationId)
    {
        this.regulationId = regulationId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getStudentName()
    {
        return studentName;
    }

    public void setStudentName(String studentName)
    {
        this.studentName = studentName;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getAcademicYear()
    {
        return academicYear;
    }

    public void setAcademicYear(String academicYear)
    {
        this.academicYear = academicYear;
    }

    public String getSemester()
    {
        return semester;
    }

    public void setSemester(String semester)
    {
        this.semester = semester;
    }

    public BigDecimal getTotalScore()
    {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore)
    {
        this.totalScore = totalScore;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime)
    {
        this.submitTime = submitTime;
    }

    public Long getReviewUserId()
    {
        return reviewUserId;
    }

    public void setReviewUserId(Long reviewUserId)
    {
        this.reviewUserId = reviewUserId;
    }

    public Date getReviewTime()
    {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime)
    {
        this.reviewTime = reviewTime;
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

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public List<Long> getMaterialIds()
    {
        return materialIds;
    }

    public void setMaterialIds(List<Long> materialIds)
    {
        this.materialIds = materialIds;
    }

    public String getRegulationTitle()
    {
        return regulationTitle;
    }

    public void setRegulationTitle(String regulationTitle)
    {
        this.regulationTitle = regulationTitle;
    }

    public List<AssessDeclarationItem> getItems()
    {
        return items;
    }

    public void setItems(List<AssessDeclarationItem> items)
    {
        this.items = items;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("declarationId", getDeclarationId())
            .append("declarationNo", getDeclarationNo())
            .append("regulationId", getRegulationId())
            .append("userId", getUserId())
            .append("studentName", getStudentName())
            .append("className", getClassName())
            .append("academicYear", getAcademicYear())
            .append("semester", getSemester())
            .append("totalScore", getTotalScore())
            .append("status", getStatus())
            .append("submitTime", getSubmitTime())
            .append("reviewUserId", getReviewUserId())
            .append("reviewTime", getReviewTime())
            .append("reviewColor", getReviewColor())
            .append("reviewComment", getReviewComment())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
