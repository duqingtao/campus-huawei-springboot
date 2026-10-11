package com.ruoyi.assess.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 材料结构化字段KV表 assess_material_field
 *
 * @author ruoyi
 */
public class AssessMaterialField extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 字段ID */
    private Long fieldId;

    /** 所属材料ID */
    private Long materialId;

    /** 字段名 */
    private String fieldKey;

    /** 字段值 */
    private String fieldValue;

    /** 字段类型(string/number/date等) */
    private String fieldType;

    /** 抽取置信度 */
    private BigDecimal confidence;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    public Long getFieldId()
    {
        return fieldId;
    }

    public void setFieldId(Long fieldId)
    {
        this.fieldId = fieldId;
    }

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

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
            .append("fieldId", getFieldId())
            .append("materialId", getMaterialId())
            .append("fieldKey", getFieldKey())
            .append("fieldValue", getFieldValue())
            .append("fieldType", getFieldType())
            .append("confidence", getConfidence())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
