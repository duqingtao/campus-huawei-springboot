package com.ruoyi.assess.service;

import java.util.List;
import com.ruoyi.assess.domain.AssessDeclarationItem;

/**
 * 综测申报明细 服务层
 *
 * @author ruoyi
 */
public interface IAssessDeclarationItemService
{
    /**
     * 按申报单ID查询明细列表
     */
    public List<AssessDeclarationItem> selectItemByDeclarationId(Long declarationId);

    /**
     * 通过明细ID查询明细信息
     */
    public AssessDeclarationItem selectItemById(Long itemId);

    /**
     * 修改综测申报明细
     */
    public int updateItem(AssessDeclarationItem item);

    /**
     * 批量逻辑删除综测申报明细
     */
    public int deleteItemByIds(Long[] itemIds);
}
