package com.ruoyi.assess.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.assess.domain.AssessRegulationClause;
import com.ruoyi.assess.mapper.AssessRegulationClauseMapper;
import com.ruoyi.assess.service.IAssessRegulationClauseService;

/**
 * 综测条款 服务层实现
 *
 * @author ruoyi
 */
@Service
public class AssessRegulationClauseServiceImpl implements IAssessRegulationClauseService
{
    @Autowired
    private AssessRegulationClauseMapper clauseMapper;

    /**
     * 查询综测条款列表
     */
    @Override
    public List<AssessRegulationClause> selectClauseList(AssessRegulationClause clause)
    {
        return clauseMapper.selectClauseList(clause);
    }

    /**
     * 通过条款ID查询条款信息
     */
    @Override
    public AssessRegulationClause selectClauseById(Long clauseId)
    {
        return clauseMapper.selectClauseById(clauseId);
    }

    /**
     * 根据细则ID查询条款树形结构
     */
    @Override
    public List<AssessRegulationClause> selectClauseTreeByRegulationId(Long regulationId)
    {
        // 查出该细则下所有条款（按排序号排序）
        List<AssessRegulationClause> allClauses = clauseMapper.selectClauseByRegulationId(regulationId);
        // 递归组装树形结构
        return buildClauseTree(allClauses);
    }

    /**
     * 递归构建条款树形结构
     *
     * @param allClauses 所有条款列表
     * @return 树形列表
     */
    private List<AssessRegulationClause> buildClauseTree(List<AssessRegulationClause> allClauses)
    {
        List<AssessRegulationClause> rootList = new ArrayList<>();
        for (AssessRegulationClause clause : allClauses)
        {
            // parentId 为 0 的是顶级条款
            if (clause.getParentId() != null && clause.getParentId().longValue() == 0L)
            {
                rootList.add(clause);
            }
        }
        for (AssessRegulationClause root : rootList)
        {
            root.setChildren(getChildren(root.getClauseId(), allClauses));
        }
        return rootList;
    }

    /**
     * 递归获取子条款
     *
     * @param parentId 父条款ID
     * @param allClauses 所有条款列表
     * @return 子条款列表
     */
    private List<AssessRegulationClause> getChildren(Long parentId, List<AssessRegulationClause> allClauses)
    {
        List<AssessRegulationClause> children = new ArrayList<>();
        for (AssessRegulationClause clause : allClauses)
        {
            if (clause.getParentId() != null && clause.getParentId().longValue() == parentId.longValue())
            {
                children.add(clause);
            }
        }
        for (AssessRegulationClause child : children)
        {
            child.setChildren(getChildren(child.getClauseId(), allClauses));
        }
        if (children.isEmpty())
        {
            return null;
        }
        return children;
    }

    /**
     * 新增综测条款
     */
    @Override
    public int insertClause(AssessRegulationClause clause)
    {
        return clauseMapper.insertClause(clause);
    }

    /**
     * 修改综测条款
     */
    @Override
    public int updateClause(AssessRegulationClause clause)
    {
        return clauseMapper.updateClause(clause);
    }

    /**
     * 批量删除综测条款
     */
    @Override
    public int deleteClauseByIds(Long[] clauseIds)
    {
        return clauseMapper.deleteClauseByIds(clauseIds);
    }
}
