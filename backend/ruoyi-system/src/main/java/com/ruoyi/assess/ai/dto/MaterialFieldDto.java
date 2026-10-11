package com.ruoyi.assess.ai.dto;

import java.math.BigDecimal;

/**
 * 材料结构化字段DTO
 * <p>
 * 对应 assess_material_field 表的一条记录(KV结构)
 *
 * @author ruoyi
 */
public class MaterialFieldDto
{
    /** 字段名(如 award_name/award_level/award_date 等) */
    private String fieldKey;

    /** 字段值 */
    private String fieldValue;

    /** 字段类型(string/number/date) */
    private String fieldType;

    /** 抽取置信度(0-1) */
    private BigDecimal confidence;

    public String getFieldKey()
    {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey)
    {
        this.fieldKey = fieldKey;
    }

    public String getFieldValue()
    {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue)
    {
        this.fieldValue = fieldValue;
    }

    public String getFieldType()
    {
        return fieldType;
    }

    public void setFieldType(String fieldType)
    {
        this.fieldType = fieldType;
    }

    public BigDecimal getConfidence()
    {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence)
    {
        this.confidence = confidence;
    }
}
