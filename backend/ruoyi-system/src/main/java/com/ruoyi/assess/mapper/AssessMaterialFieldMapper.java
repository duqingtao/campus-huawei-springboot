package com.ruoyi.assess.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.assess.domain.AssessMaterialField;

/**
 * 材料结构化字段 数据层
 *
 * @author ruoyi
 */
public interface AssessMaterialFieldMapper
{
    /**
     * 按材料ID查询所有有效字段
     */
    public List<AssessMaterialField> selectFieldByMaterialId(Long materialId);

    /**
     * 按材料ID逻辑删除所有字段(先删旧)
     */
    public int deleteFieldByMaterialId(Long materialId);

    /**
     * 批量新增字段(后插新)
     */
    public int batchInsertField(List<AssessMaterialField> fields);
}
