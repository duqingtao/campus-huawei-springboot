package com.ruoyi.assess.mapper;

import java.util.List;
import com.ruoyi.assess.domain.AssessMaterial;

/**
 * 综测材料 数据层
 *
 * @author ruoyi
 */
public interface AssessMaterialMapper
{
    /**
     * 查询综测材料列表
     *
     * @param material 综测材料
     * @return 综测材料集合
     */
    public List<AssessMaterial> selectMaterialList(AssessMaterial material);

    /**
     * 通过材料ID查询材料信息
     *
     * @param materialId 材料ID
     * @return 综测材料对象
     */
    public AssessMaterial selectMaterialById(Long materialId);

    /**
     * 新增综测材料
     *
     * @param material 综测材料
     * @return 结果
     */
    public int insertMaterial(AssessMaterial material);

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
     * @param material 综测材料（含materialId和updateBy）
     * @return 结果
     */
    public int reprocessMaterial(AssessMaterial material);
}
