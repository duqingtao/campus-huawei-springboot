package com.ruoyi.assess.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;

/**
 * 班级统计学生申报明细 DTO
 * <p>
 * 用于学生明细列表和汇总导出，不映射数据库表
 *
 * @author ruoyi
 */
public class AssessStatsStudent
{
    /** 申报单ID */
    @Excel(name = "申报单ID")
    private Long declarationId;

    /** 申报单号 */
    @Excel(name = "申报单号")
    private String declarationNo;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String studentName;

    /** 学号(sys_user.user_name) */
    @Excel(name = "学号")
    private String studentNo;

    /** 班级名称 */
    @Excel(name = "班级名称")
    private String className;

    /** 学年 */
    @Excel(name = "学年")
    private String academicYear;

    /** 学期(1上学期 2下学期) */
    @Excel(name = "学期", readConverterExp = "1=上学期,2=下学期")
    private String semester;

    /** 状态(0草稿 1已提交 2审核中 3部分通过 4全部通过 5已驳回) */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=已提交,2=审核中,3=部分通过,4=全部通过,5=已驳回")
    private String status;

    /** 总分 */
    @Excel(name = "总分")
    private BigDecimal totalScore;

    // ========== 以下为 transient 查询辅助字段 ==========

    /** 数据隔离：部门ID(班委按dept_id过滤) */
    private Long deptId;

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

    public String getStudentName()
    {
        return studentName;
    }

    public void setStudentName(String studentName)
    {
        this.studentName = studentName;
    }

    public String getStudentNo()
    {
        return studentNo;
    }

    public void setStudentNo(String studentNo)
    {
        this.studentNo = studentNo;
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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public BigDecimal getTotalScore()
    {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore)
    {
        this.totalScore = totalScore;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("declarationId", getDeclarationId())
            .append("declarationNo", getDeclarationNo())
            .append("studentName", getStudentName())
            .append("studentNo", getStudentNo())
            .append("className", getClassName())
            .append("academicYear", getAcademicYear())
            .append("semester", getSemester())
            .append("status", getStatus())
            .append("totalScore", getTotalScore())
            .toString();
    }
}
