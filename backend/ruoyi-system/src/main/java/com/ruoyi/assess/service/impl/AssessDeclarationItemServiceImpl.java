package com.ruoyi.assess.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.assess.domain.AssessDeclarationItem;
import com.ruoyi.assess.mapper.AssessDeclarationItemMapper;
import com.ruoyi.assess.service.IAssessDeclarationItemService;

/**
 * 综测申报明细 服务层实现
 *
 * @author ruoyi
 */
@Service
public class AssessDeclarationItemServiceImpl implements IAssessDeclarationItemService
{
    @Autowired
    private AssessDeclarationItemMapper itemMapper;

    @Override
    public List<AssessDeclarationItem> selectItemByDeclarationId(Long declarationId)
    {
        return itemMapper.selectItemByDeclarationId(declarationId);
    }

    @Override
    public AssessDeclarationItem selectItemById(Long itemId)
    {
        return itemMapper.selectItemById(itemId);
    }

    @Override
    public int updateItem(AssessDeclarationItem item)
    {
        return itemMapper.updateItem(item);
    }

    @Override
    public int deleteItemByIds(Long[] itemIds)
    {
        return itemMapper.deleteItemByIds(itemIds);
    }
}
