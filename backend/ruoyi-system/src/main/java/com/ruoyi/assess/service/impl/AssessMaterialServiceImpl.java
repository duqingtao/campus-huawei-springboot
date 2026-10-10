package com.ruoyi.assess.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.assess.domain.AssessMaterial;
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
    @Autowired
    private AssessMaterialMapper materialMapper;

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
     * 重新识别：重置OCR状态和抽取状态
     */
    @Override
    public int reprocessMaterial(Long materialId, String username)
    {
        AssessMaterial material = materialMapper.selectMaterialById(materialId);
        if (material == null)
        {
            throw new ServiceException("材料不存在或已被删除");
        }

        AssessMaterial update = new AssessMaterial();
        update.setMaterialId(materialId);
        update.setOcrStatus("0");
        update.setOcrText(null);
        update.setExtractStatus("0");
        update.setUpdateBy(username);
        return materialMapper.reprocessMaterial(update);
    }
}
