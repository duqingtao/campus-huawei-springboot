package com.ruoyi.assess.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 综测细则主表 assess_regulation
 *
 * @author ruoyi
 */
public class AssessRegulation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 细则ID */
    @Excel(name = "细则ID")
    private Long regulationId;

    /** 细则编号 */
    @Excel(name = "细则编号")
    private String regulationNo;

    /** 细则标题 */
    @Excel(name = "细则标题")
    private String title;

    /** 发布日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发布日期", width = 20)
    private Date publishDate;

    /** 生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期", width = 20)
    private Date effectiveDate;

    /** 失效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "失效日期", width = 20)
    private Date expireDate;

    /** 状态(0草案 1已发布 2已废止) */
    @Excel(name = "状态", readConverterExp = "0=草案,1=已发布,2=已废止")
    private String status;

    /** 版本号 */
    @Excel(name = "版本号")
    private Integer versionNum;

    /** 细则内容(富文本) */
    private String content;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    public Long getRegulationId()
    {
        return regulationId;
    }

    public void setRegulationId(Long regulationId)
    {
        this.regulationId = regulationId;
    }

    public String getRegulationNo()
    {
        return regulationNo;
    }

    public void setRegulationNo(String regulationNo)
    {
        this.regulationNo = regulationNo;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Date getPublishDate()
    {
        return publishDate;
    }

    public void setPublishDate(Date publishDate)
    {
        this.publishDate = publishDate;
    }

    public Date getEffectiveDate()
    {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate)
    {
        this.effectiveDate = effectiveDate;
    }

    public Date getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate(Date expireDate)
    {
        this.expireDate = expireDate;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getVersionNum()
    {
        return versionNum;
    }

    public void setVersionNum(Integer versionNum)
    {
        this.versionNum = versionNum;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
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
            .append("regulationId", getRegulationId())
            .append("regulationNo", getRegulationNo())
            .append("title", getTitle())
            .append("publishDate", getPublishDate())
            .append("effectiveDate", getEffectiveDate())
            .append("expireDate", getExpireDate())
            .append("status", getStatus())
            .append("versionNum", getVersionNum())
            .append("content", getContent())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
