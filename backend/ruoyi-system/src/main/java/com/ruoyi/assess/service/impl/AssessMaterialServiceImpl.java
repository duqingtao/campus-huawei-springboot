package com.ruoyi.assess.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.assess.ai.MaterialOcrService;
import com.ruoyi.assess.ai.FieldExtractService;
import com.ruoyi.assess.ai.dto.MaterialFieldDto;
import com.ruoyi.assess.ai.dto.OcrResult;
import com.ruoyi.assess.domain.AssessMaterial;
import com.ruoyi.assess.domain.AssessMaterialField;
import com.ruoyi.assess.mapper.AssessMaterialFieldMapper;
import com.ruoyi.assess.mapper.AssessMaterialMapper;
import com.ruoyi.assess.service.IAssessMaterialService;

/**
 * 综测材料 服务层实现
 *
 * @author ruoyi
 */
@Service
public class AssessMaterialServiceImpl implements IAssessMaterialService
{
    private static final Logger log = LoggerFactory.getLogger(AssessMaterialServiceImpl.class);

    @Autowired
    private AssessMaterialMapper materialMapper;

    @Autowired
    private AssessMaterialFieldMapper materialFieldMapper;

    @Autowired
    private MaterialOcrService materialOcrService;

    @Autowired
    private FieldExtractService fieldExtractService;

    /**
     * 查询综测材料列表（含数据隔离）
     */
    @Override
    public List<AssessMaterial> selectMaterialList(AssessMaterial material)
    {
        // 数据隔离：根据当前用户角色注入过滤条件
        LoginUser loginUser = SecurityUtils.getLoginUser();
        List<SysRole> roles = loginUser.getUser().getRoles();

        boolean isStudent = roles != null && roles.stream()
                .anyMatch(r -> "assess_student".equals(r.getRoleKey()));
        boolean isCommittee = roles != null && roles.stream()
                .anyMatch(r -> "assess_committee".equals(r.getRoleKey()));

        if (SecurityUtils.isAdmin())
        {
            // 管理员：不加过滤，看全部
        }
        else if (isStudent)
        {
            // 学生：只能查自己的材料
            material.setUserId(SecurityUtils.getUserId());
        }
        else if (isCommittee)
        {
            // 班委：按 dept_id 查本班材料
            material.setDeptId(SecurityUtils.getDeptId());
        }
        // 辅导员（assess_counselor, data_scope=1）：不加过滤，看全部

        return materialMapper.selectMaterialList(material);
    }

    /**
     * 通过材料ID查询材料信息
     */
    @Override
    public AssessMaterial selectMaterialById(Long materialId)
    {
        return materialMapper.selectMaterialById(materialId);
    }

    /**
     * 上传登记新增综测材料
     */
    @Override
    public int insertMaterial(MultipartFile file, AssessMaterial material, String username)
    {
        try
        {
            // 上传文件到本地（mock OBS，只存本地路径）
            String filePath = RuoYiConfig.getUploadPath();
            String fileName = FileUploadUtils.upload(filePath, file);

            // 按扩展名判断文件类型
            String originalFilename = file.getOriginalFilename();
            String fileType = "image";
            if (originalFilename != null && originalFilename.contains("."))
            {
                String extension = originalFilename
                        .substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
                if ("pdf".equals(extension))
                {
                    fileType = "pdf";
                }
            }

            // 从当前登录用户获取学生信息
            LoginUser loginUser = SecurityUtils.getLoginUser();
            material.setUserId(loginUser.getUserId());
            material.setDeptId(loginUser.getDeptId());
            material.setStudentNo(loginUser.getUser().getUserName());
            material.setStudentName(loginUser.getUser().getNickName());
            if (loginUser.getUser().getDept() != null)
            {
                material.setClassName(loginUser.getUser().getDept().getDeptName());
            }

            // 文件信息
            // 材料名称为空时用原始文件名自动填充
            if (StringUtils.isEmpty(material.getMaterialName()))
            {
                material.setMaterialName(originalFilename);
            }
            material.setFileUrl(fileName);
            material.setFileType(fileType);
            material.setFileSize(file.getSize());
            material.setOcrStatus("0");
            material.setExtractStatus("0");
            material.setCreateBy(username);

            return materialMapper.insertMaterial(material);
        }
        catch (Exception e)
        {
            throw new ServiceException("上传材料文件失败：" + e.getMessage());
        }
    }

    /**
     * 修改综测材料
     */
    @Override
    public int updateMaterial(AssessMaterial material)
    {
        return materialMapper.updateMaterial(material);
    }

    /**
     * 批量逻辑删除综测材料
     */
    @Override
    public int deleteMaterialByIds(Long[] materialIds)
    {
        return materialMapper.deleteMaterialByIds(materialIds);
    }

    /**
     * 重新识别：重置OCR状态和抽取状态后，同步触发OCR → 字段抽取
     * <p>
     * 触发失败只记remark，不影响状态重置本身，不抛500。
     */
    @Override
    public int reprocessMaterial(Long materialId, String username)
    {
        AssessMaterial material = materialMapper.selectMaterialById(materialId);
        if (material == null)
        {
            throw new ServiceException("材料不存在或已被删除");
        }

        // Step 1: 重置状态(原有逻辑)
        AssessMaterial reset = new AssessMaterial();
        reset.setMaterialId(materialId);
        reset.setOcrStatus("0");
        reset.setOcrText(null);
        reset.setExtractStatus("0");
        reset.setUpdateBy(username);
        materialMapper.reprocessMaterial(reset);

        // Step 2: 同步触发OCR → 字段抽取
        // 触发失败只记remark，不影响状态重置本身
        try
        {
            triggerOcrAndExtract(material, username);
        }
        catch (Exception e)
        {
            log.error("材料[{}]OCR/字段抽取触发异常，不影响状态重置", materialId, e);
            AssessMaterial failUpdate = new AssessMaterial();
            failUpdate.setMaterialId(materialId);
            failUpdate.setOcrStatus("3");
            failUpdate.setExtractStatus("3");
            failUpdate.setRemark("OCR/抽取触发异常：" + e.getMessage());
            failUpdate.setUpdateBy(username);
            materialMapper.updateMaterial(failUpdate);
        }

        return 1;
    }

    /**
     * 同步执行OCR → 字段抽取链路
     */
    private void triggerOcrAndExtract(AssessMaterial material, String username)
    {
        Long materialId = material.getMaterialId();

        // ── OCR ──
        AssessMaterial ocrUpdate = new AssessMaterial();
        ocrUpdate.setMaterialId(materialId);
        ocrUpdate.setOcrStatus("1");
        ocrUpdate.setUpdateBy(username);
        materialMapper.updateMaterial(ocrUpdate);

        OcrResult ocrResult = materialOcrService.processOcr(material);

        if (ocrResult == null || !ocrResult.isSuccess())
        {
            String errorMsg = ocrResult != null ? ocrResult.getErrorMessage() : "OCR返回null";
            log.warn("材料[{}]OCR失败: {}", materialId, errorMsg);
            AssessMaterial failUpdate = new AssessMaterial();
            failUpdate.setMaterialId(materialId);
            failUpdate.setOcrStatus("3");
            failUpdate.setRemark("OCR失败：" + errorMsg);
            failUpdate.setUpdateBy(username);
            materialMapper.updateMaterial(failUpdate);
            return;
        }

        // OCR成功：写ocr_text、ocr_status='2'
        AssessMaterial ocrSuccess = new AssessMaterial();
        ocrSuccess.setMaterialId(materialId);
        ocrSuccess.setOcrStatus("2");
        ocrSuccess.setOcrText(ocrResult.getOcrText());
        ocrSuccess.setUpdateBy(username);
        materialMapper.updateMaterial(ocrSuccess);
        log.info("材料[{}]OCR成功，文本长度={}", materialId, ocrResult.getOcrText().length());

        // ── 字段抽取 ──
        AssessMaterial extractUpdate = new AssessMaterial();
        extractUpdate.setMaterialId(materialId);
        extractUpdate.setExtractStatus("1");
        extractUpdate.setUpdateBy(username);
        materialMapper.updateMaterial(extractUpdate);

        List<MaterialFieldDto> fields = fieldExtractService.extractFields(
                ocrResult.getOcrText(), material.getMaterialType());

        if (fields == null || fields.isEmpty())
        {
            log.info("材料[{}]字段抽取结果为空", materialId);
            AssessMaterial emptyExtract = new AssessMaterial();
            emptyExtract.setMaterialId(materialId);
            emptyExtract.setExtractStatus("3");
            emptyExtract.setRemark("字段抽取结果为空");
            emptyExtract.setUpdateBy(username);
            materialMapper.updateMaterial(emptyExtract);
            return;
        }

        // 先删旧字段再插新
        materialFieldMapper.deleteFieldByMaterialId(materialId);

        List<AssessMaterialField> fieldEntities = new ArrayList<>();
        for (MaterialFieldDto dto : fields)
        {
            AssessMaterialField field = new AssessMaterialField();
            field.setMaterialId(materialId);
            field.setFieldKey(dto.getFieldKey());
            field.setFieldValue(dto.getFieldValue());
            field.setFieldType(dto.getFieldType());
            field.setConfidence(dto.getConfidence());
            field.setCreateBy(username);
            fieldEntities.add(field);
        }
        materialFieldMapper.batchInsertField(fieldEntities);

        // 抽取成功
        AssessMaterial extractSuccess = new AssessMaterial();
        extractSuccess.setMaterialId(materialId);
        extractSuccess.setExtractStatus("2");
        extractSuccess.setUpdateBy(username);
        materialMapper.updateMaterial(extractSuccess);
        log.info("材料[{}]字段抽取成功，共{}个字段", materialId, fields.size());
    }
}
