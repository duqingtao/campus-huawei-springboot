package com.ruoyi.assess.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.assess.domain.AssessDeclarationItem;

/**
 * 综测申报明细 数据层
 *
 * @author ruoyi
 */
public interface AssessDeclarationItemMapper
{
    /**
     * 按申报单ID查询明细列表(含材料名称/条款编号JOIN)
     */
    public List<AssessDeclarationItem> selectItemByDeclarationId(Long declarationId);

    /**
     * 通过明细ID查询明细信息
     */
    public AssessDeclarationItem selectItemById(Long itemId);

    /**
     * 新增综测申报明细
     */
    public int insertItem(AssessDeclarationItem item);

    /**
     * 批量新增综测申报明细
     */
    public int batchInsertItem(List<AssessDeclarationItem> items);

    /**
     * 修改综测申报明细
     */
    public int updateItem(AssessDeclarationItem item);

    /**
     * 批量逻辑删除综测申报明细
     */
    public int deleteItemByIds(Long[] itemIds);

    /**
     * 按申报单ID逻辑删除所有明细
     */
    public int deleteItemByDeclarationId(Long declarationId);

    /**
     * 查询材料结构化字段值(供时间窗规则使用)
     *
     * @param materialId 材料ID
     * @param fieldKey   字段名(如 award_date)
     * @return 字段值
     */
    public String selectMaterialFieldValue(@Param("materialId") Long materialId,
                                           @Param("fieldKey") String fieldKey);
}
