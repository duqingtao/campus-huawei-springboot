package com.ruoyi.assess.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.assess.domain.AssessMaterial;

/**
 * 综测材料 服务层
 *
 * @author ruoyi
 */
public interface IAssessMaterialService
{
    /**
     * 查询综测材料列表（含数据隔离）
     *
     * @param material 综测材料
     * @return 材料集合
     */
    public List<AssessMaterial> selectMaterialList(AssessMaterial material);

    /**
     * 通过材料ID查询材料信息
     *
     * @param materialId 材料ID
     * @return 材料对象
     */
    public AssessMaterial selectMaterialById(Long materialId);

    /**
     * 上传登记新增综测材料
     *
     * @param file 上传文件
     * @param material 材料信息（materialName, materialType）
     * @param username 操作人用户名
     * @return 结果
     */
    public int insertMaterial(MultipartFile file, AssessMaterial material, String username);

    /**
     * 修改综测材料
     *
     * @param material 综测材料
     * @return 结果
     */
    public int updateMaterial(AssessMaterial material);

    /**
     * 批量逻辑删除综测材料
     *
     * @param materialIds 材料ID数组
     * @return 结果
     */
    public int deleteMaterialByIds(Long[] materialIds);

    /**
     * 重新识别：重置OCR状态和抽取状态
     *
     * @param materialId 材料ID
     * @param username 操作人用户名
     * @return 结果
     */
    public int reprocessMaterial(Long materialId, String username);
}
