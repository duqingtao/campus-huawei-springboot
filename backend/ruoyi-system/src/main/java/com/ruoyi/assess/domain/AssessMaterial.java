package com.ruoyi.assess.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 综测材料主表 assess_material
 *
 * @author ruoyi
 */
public class AssessMaterial extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 材料ID */
    @Excel(name = "材料ID")
    private Long materialId;

    /** 上传学生用户ID */
    private Long userId;

    /** 学号 */
    @Excel(name = "学号")
    private String studentNo;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String studentName;

    /** 班级名称 */
    @Excel(name = "班级名称")
    private String className;

    /** 班级部门ID(数据隔离) */
    private Long deptId;

    /** 材料名称 */
    @Excel(name = "材料名称")
    private String materialName;

    /** 材料类型(证书/证明/报告等) */
    @Excel(name = "材料类型")
    private String materialType;

    /** 文件存储URL */
    @Excel(name = "文件URL")
    private String fileUrl;

    /** 文件类型(image/pdf) */
    @Excel(name = "文件类型")
    private String fileType;

    /** 文件大小(字节) */
    @Excel(name = "文件大小")
    private Long fileSize;

    /** OCR状态(0未识别 1识别中 2已识别 3失败) */
    @Excel(name = "OCR状态", readConverterExp = "0=未识别,1=识别中,2=已识别,3=失败")
    private String ocrStatus;

    /** OCR识别全文 */
    private String ocrText;

    /** 抽取状态(0未抽取 1抽取中 2已抽取 3失败) */
    @Excel(name = "抽取状态", readConverterExp = "0=未抽取,1=抽取中,2=已抽取,3=失败")
    private String extractStatus;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getStudentNo()
    {
        return studentNo;
    }

    public void setStudentNo(String studentNo)
    {
        this.studentNo = studentNo;
    }

    public String getStudentName()
    {
        return studentName;
    }

    public void setStudentName(String studentName)
    {
        this.studentName = studentName;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
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

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public String getOcrStatus()
    {
        return ocrStatus;
    }

    public void setOcrStatus(String ocrStatus)
    {
        this.ocrStatus = ocrStatus;
    }

    public String getOcrText()
    {
        return ocrText;
    }

    public void setOcrText(String ocrText)
    {
        this.ocrText = ocrText;
    }

    public String getExtractStatus()
    {
        return extractStatus;
    }

    public void setExtractStatus(String extractStatus)
    {
        this.extractStatus = extractStatus;
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
            .append("materialId", getMaterialId())
            .append("userId", getUserId())
            .append("studentNo", getStudentNo())
            .append("studentName", getStudentName())
            .append("className", getClassName())
            .append("deptId", getDeptId())
            .append("materialName", getMaterialName())
            .append("materialType", getMaterialType())
            .append("fileUrl", getFileUrl())
            .append("fileType", getFileType())
            .append("fileSize", getFileSize())
            .append("ocrStatus", getOcrStatus())
            .append("ocrText", getOcrText())
            .append("extractStatus", getExtractStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
