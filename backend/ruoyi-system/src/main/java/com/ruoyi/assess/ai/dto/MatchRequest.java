package com.ruoyi.assess.ai.dto;

/**
 * AI条款匹配入参DTO
 * <p>
 * 对应 design.md Step2 输入中单个材料的匹配请求
 *
 * @author ruoyi
 */
public class MatchRequest
{
    /** 材料ID */
    private Long materialId;

    /** 材料名称 */
    private String materialName;

    /** 材料类型(竞赛证书/志愿证明/论文录用等) */
    private String materialType;

    /** OCR识别全文(Mock用于关键词匹配) */
    private String ocrText;

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
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

    public String getOcrText()
    {
        return ocrText;
    }

    public void setOcrText(String ocrText)
    {
        this.ocrText = ocrText;
    }
}
