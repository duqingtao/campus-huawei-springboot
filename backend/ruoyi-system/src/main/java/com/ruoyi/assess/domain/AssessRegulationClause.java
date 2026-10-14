package com.ruoyi.assess.domain;

import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 综测条款表 assess_regulation_clause
 *
 * @author ruoyi
 */
public class AssessRegulationClause extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 条款ID */
    @Excel(name = "条款ID")
    private Long clauseId;

    /** 所属细则ID */
    @Excel(name = "所属细则ID")
    private Long regulationId;

    /** 父条款ID(0=顶级条款) */
    @Excel(name = "父条款ID")
    private Long parentId;

    /** 条款编号 */
    @Excel(name = "条款编号")
    private String clauseNo;

    /** 条款标题 */
    @Excel(name = "条款标题")
    private String clauseTitle;

    /** 条款原文 */
    private String clauseText;

    /** 条款类型(0基础项 1加分项 2扣分项) */
    @Excel(name = "条款类型", readConverterExp = "0=基础项,1=加分项,2=扣分项")
    private String clauseType;

    /** 最高分值 */
    @Excel(name = "最高分值")
    private BigDecimal maxScore;

    /** 排序号 */
    @Excel(name = "排序号")
    private Integer sortOrder;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    /** 子条款列表(树形结构) */
    private List<AssessRegulationClause> children;

    /** 预览标记：该条数据是否无效(缺编号或标题) */
    private Boolean invalid;

    /** 预览标记：无效原因 */
    private String invalidReason;

    public Long getClauseId()
    {
        return clauseId;
    }

    public void setClauseId(Long clauseId)
    {
        this.clauseId = clauseId;
    }

    public Long getRegulationId()
    {
        return regulationId;
    }

    public void setRegulationId(Long regulationId)
    {
        this.regulationId = regulationId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getClauseNo()
    {
        return clauseNo;
    }

    public void setClauseNo(String clauseNo)
    {
        this.clauseNo = clauseNo;
    }

    public String getClauseTitle()
    {
        return clauseTitle;
    }

    public void setClauseTitle(String clauseTitle)
    {
        this.clauseTitle = clauseTitle;
    }

    public String getClauseText()
    {
        return clauseText;
    }

    public void setClauseText(String clauseText)
    {
        this.clauseText = clauseText;
    }

    public String getClauseType()
    {
        return clauseType;
    }

    public void setClauseType(String clauseType)
    {
        this.clauseType = clauseType;
    }

    public BigDecimal getMaxScore()
    {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore)
    {
        this.maxScore = maxScore;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public List<AssessRegulationClause> getChildren()
    {
        return children;
    }

    public void setChildren(List<AssessRegulationClause> children)
    {
        this.children = children;
    }

    public Boolean getInvalid()
    {
        return invalid;
    }

    public void setInvalid(Boolean invalid)
    {
        this.invalid = invalid;
    }

    public String getInvalidReason()
    {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason)
    {
        this.invalidReason = invalidReason;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("clauseId", getClauseId())
            .append("regulationId", getRegulationId())
            .append("parentId", getParentId())
            .append("clauseNo", getClauseNo())
            .append("clauseTitle", getClauseTitle())
            .append("clauseText", getClauseText())
            .append("clauseType", getClauseType())
            .append("maxScore", getMaxScore())
            .append("sortOrder", getSortOrder())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
