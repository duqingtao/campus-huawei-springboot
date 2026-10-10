package com.ruoyi.assess.engine;

import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 规则引擎对单条明细的校验结果
 *
 * @author ruoyi
 */
public class ItemRuleResult
{
    /** 明细ID */
    private Long itemId;

    /** 关联材料ID */
    private Long materialId;

    /** 关联条款ID */
    private Long clauseId;

    /** 条款编号 */
    private String clauseNo;

    /** 条款类型 */
    private String clauseType;

    /** AI建议分(原始) */
    private BigDecimal suggestedScore;

    /** 规则引擎调整后分值 */
    private BigDecimal ruleAdjustedScore;

    /** 最终入库分值 */
    private BigDecimal finalScore;

    /** 各规则校验明细 */
    private List<CheckDetail> checkDetails;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public Long getClauseId()
    {
        return clauseId;
    }

    public void setClauseId(Long clauseId)
    {
        this.clauseId = clauseId;
    }

    public String getClauseNo()
    {
        return clauseNo;
    }

    public void setClauseNo(String clauseNo)
    {
        this.clauseNo = clauseNo;
    }

    public String getClauseType()
    {
        return clauseType;
    }

    public void setClauseType(String clauseType)
    {
        this.clauseType = clauseType;
    }

    public BigDecimal getSuggestedScore()
    {
        return suggestedScore;
    }

    public void setSuggestedScore(BigDecimal suggestedScore)
    {
        this.suggestedScore = suggestedScore;
    }

    public BigDecimal getRuleAdjustedScore()
    {
        return ruleAdjustedScore;
    }

    public void setRuleAdjustedScore(BigDecimal ruleAdjustedScore)
    {
        this.ruleAdjustedScore = ruleAdjustedScore;
    }

    public BigDecimal getFinalScore()
    {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore)
    {
        this.finalScore = finalScore;
    }

    public List<CheckDetail> getCheckDetails()
    {
        return checkDetails;
    }

    public void setCheckDetails(List<CheckDetail> checkDetails)
    {
        this.checkDetails = checkDetails;
    }

    /**
     * 添加一条校验明细
     */
    public void addCheckDetail(String ruleType, boolean passed, String message)
    {
        if (checkDetails == null)
        {
            checkDetails = new java.util.ArrayList<>();
        }
        checkDetails.add(new CheckDetail(ruleType, passed, message));
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("itemId", getItemId())
            .append("clauseId", getClauseId())
            .append("suggestedScore", getSuggestedScore())
            .append("ruleAdjustedScore", getRuleAdjustedScore())
            .append("finalScore", getFinalScore())
            .append("checkDetails", getCheckDetails())
            .toString();
    }
}
