package com.ruoyi.assess.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 综测申报明细表 assess_declaration_item
 *
 * @author ruoyi
 */
public class AssessDeclarationItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    @Excel(name = "明细ID")
    private Long itemId;

    /** 申报单ID */
    @Excel(name = "申报单ID")
    private Long declarationId;

    /** 关联材料ID */
    @Excel(name = "材料ID")
    private Long materialId;

    /** 关联条款ID */
    @Excel(name = "条款ID")
    private Long clauseId;

    /** AI建议分值(不入库,仅参考) */
    @Excel(name = "AI建议分")
    private BigDecimal suggestedScore;

    /** 规则引擎调整后分值 */
    @Excel(name = "规则调整分")
    private BigDecimal ruleAdjustedScore;

    /** 最终入库分值 */
    @Excel(name = "最终分值")
    private BigDecimal finalScore;

    /** 证据-条款编号 */
    @Excel(name = "证据条款编号")
    private String evidenceClauseNo;

    /** 证据-条款原文 */
    private String evidenceClauseText;

    /** 证据-匹配置信度 */
    @Excel(name = "置信度")
    private BigDecimal evidenceConfidence;

    /** 匹配原因说明 */
    private String matchReason;

    /** 匹配状态(0待匹配 1已匹配 2未匹配 3存疑) */
    @Excel(name = "匹配状态", readConverterExp = "0=待匹配,1=已匹配,2=未匹配,3=存疑")
    private String matchStatus;

    /** 规则校验结果(JSON:封顶/互斥/时间窗) */
    private String ruleCheckResult;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    // ========== 以下为 transient JOIN 查询辅助字段 ==========

    /** 材料名称(JOIN assess_material) */
    @Excel(name = "材料名称")
    private String materialName;

    /** 材料类型(JOIN assess_material) */
    @Excel(name = "材料类型")
    private String materialType;

    /** 条款编号(JOIN assess_regulation_clause) */
    @Excel(name = "条款编号")
    private String clauseNo;

    /** 条款标题(JOIN assess_regulation_clause) */
    @Excel(name = "条款标题")
    private String clauseTitle;

    /** 条款最高分值(JOIN assess_regulation_clause) */
    private BigDecimal maxScore;

    /** 条款类型(JOIN assess_regulation_clause) */
    private String clauseType;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getDeclarationId()
    {
        return declarationId;
    }

    public void setDeclarationId(Long declarationId)
    {
        this.declarationId = declarationId;
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

    public String getEvidenceClauseNo()
    {
        return evidenceClauseNo;
    }

    public void setEvidenceClauseNo(String evidenceClauseNo)
    {
        this.evidenceClauseNo = evidenceClauseNo;
    }

    public String getEvidenceClauseText()
    {
        return evidenceClauseText;
    }

    public void setEvidenceClauseText(String evidenceClauseText)
    {
        this.evidenceClauseText = evidenceClauseText;
    }

    public BigDecimal getEvidenceConfidence()
    {
        return evidenceConfidence;
    }

    public void setEvidenceConfidence(BigDecimal evidenceConfidence)
    {
        this.evidenceConfidence = evidenceConfidence;
    }

    public String getMatchReason()
    {
        return matchReason;
    }

    public void setMatchReason(String matchReason)
    {
        this.matchReason = matchReason;
    }

    public String getMatchStatus()
    {
        return matchStatus;
    }

    public void setMatchStatus(String matchStatus)
    {
        this.matchStatus = matchStatus;
    }

    public String getRuleCheckResult()
    {
        return ruleCheckResult;
    }

    public void setRuleCheckResult(String ruleCheckResult)
    {
        this.ruleCheckResult = ruleCheckResult;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getMaterialType()
    {
        return materialType;
    }

    public void setMaterialType(String materialType)
    {
        this.materialType = materialType;
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

    public BigDecimal getMaxScore()
    {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore)
    {
        this.maxScore = maxScore;
    }

    public String getClauseType()
    {
        return clauseType;
    }

    public void setClauseType(String clauseType)
    {
        this.clauseType = clauseType;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("itemId", getItemId())
            .append("declarationId", getDeclarationId())
            .append("materialId", getMaterialId())
            .append("clauseId", getClauseId())
            .append("suggestedScore", getSuggestedScore())
            .append("ruleAdjustedScore", getRuleAdjustedScore())
            .append("finalScore", getFinalScore())
            .append("evidenceClauseNo", getEvidenceClauseNo())
            .append("evidenceClauseText", getEvidenceClauseText())
            .append("evidenceConfidence", getEvidenceConfidence())
            .append("matchReason", getMatchReason())
            .append("matchStatus", getMatchStatus())
            .append("ruleCheckResult", getRuleCheckResult())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
