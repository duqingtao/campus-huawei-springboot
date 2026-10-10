package com.ruoyi.assess.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 综测规则引擎配置表 assess_rule_config
 *
 * @author ruoyi
 */
public class AssessRuleConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 规则ID */
    @Excel(name = "规则ID")
    private Long ruleId;

    /** 所属细则ID */
    @Excel(name = "细则ID")
    private Long regulationId;

    /** 规则名称 */
    @Excel(name = "规则名称")
    private String ruleName;

    /** 规则类型(cap封顶 exclude互斥 time_window时间窗) */
    @Excel(name = "规则类型", readConverterExp = "cap=封顶,mutex=互斥,time_window=时间窗")
    private String ruleType;

    /** 规则描述 */
    @Excel(name = "规则描述")
    private String ruleDesc;

    /** 规则条件(JSON) */
    private String ruleCondition;

    /** 规则动作(JSON) */
    private String ruleAction;

    /** 优先级(数值越小越先执行) */
    @Excel(name = "优先级")
    private Integer priority;

    /** 状态(0禁用 1启用) */
    @Excel(name = "状态", readConverterExp = "0=禁用,1=启用")
    private String status;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    public Long getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(Long ruleId)
    {
        this.ruleId = ruleId;
    }

    public Long getRegulationId()
    {
        return regulationId;
    }

    public void setRegulationId(Long regulationId)
    {
        this.regulationId = regulationId;
    }

    public String getRuleName()
    {
        return ruleName;
    }

    public void setRuleName(String ruleName)
    {
        this.ruleName = ruleName;
    }

    public String getRuleType()
    {
        return ruleType;
    }

    public void setRuleType(String ruleType)
    {
        this.ruleType = ruleType;
    }

    public String getRuleDesc()
    {
        return ruleDesc;
    }

    public void setRuleDesc(String ruleDesc)
    {
        this.ruleDesc = ruleDesc;
    }

    public String getRuleCondition()
    {
        return ruleCondition;
    }

    public void setRuleCondition(String ruleCondition)
    {
        this.ruleCondition = ruleCondition;
    }

    public String getRuleAction()
    {
        return ruleAction;
    }

    public void setRuleAction(String ruleAction)
    {
        this.ruleAction = ruleAction;
    }

    public Integer getPriority()
    {
        return priority;
    }

    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
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
            .append("ruleId", getRuleId())
            .append("regulationId", getRegulationId())
            .append("ruleName", getRuleName())
            .append("ruleType", getRuleType())
            .append("ruleDesc", getRuleDesc())
            .append("ruleCondition", getRuleCondition())
            .append("ruleAction", getRuleAction())
            .append("priority", getPriority())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
