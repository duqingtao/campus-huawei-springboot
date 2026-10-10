package com.ruoyi.assess.ai.dto;

import java.math.BigDecimal;

/**
 * AI条款匹配出参DTO
 * <p>
 * 对应 design.md Step2 输出，Mock实现只产出Top1候选
 *
 * @author ruoyi
 */
public class MatchResponse
{
    /** 材料ID */
    private Long materialId;

    /** 匹配到的条款ID(null=未匹配) */
    private Long clauseId;

    /** AI建议分值(不超过条款maxScore) */
    private BigDecimal suggestedScore;

    /** 证据-条款编号 */
    private String evidenceClauseNo;

    /** 证据-条款原文 */
    private String evidenceClauseText;

    /** 证据-匹配置信度 */
    private BigDecimal evidenceConfidence;

    /** 匹配原因说明 */
    private String matchReason;

    /** 匹配状态(1已匹配 2未匹配) */
    private String matchStatus;

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
}
