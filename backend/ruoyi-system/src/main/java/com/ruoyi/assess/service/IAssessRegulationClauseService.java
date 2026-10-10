package com.ruoyi.assess.service;

import java.util.List;
import com.ruoyi.assess.domain.AssessRegulationClause;

/**
 * 综测条款 服务层
 *
 * @author ruoyi
 */
public interface IAssessRegulationClauseService
{
    /**
     * 查询综测条款列表
     *
     * @param clause 综测条款
     * @return 条款集合
     */
    public List<AssessRegulationClause> selectClauseList(AssessRegulationClause clause);

    /**
     * 通过条款ID查询条款信息
     *
     * @param clauseId 条款ID
     * @return 条款对象
     */
    public AssessRegulationClause selectClauseById(Long clauseId);

    /**
     * 根据细则ID查询条款树形结构
     *
     * @param regulationId 细则ID
     * @return 条款树形列表
     */
    public List<AssessRegulationClause> selectClauseTreeByRegulationId(Long regulationId);

    /**
     * 新增综测条款
     *
     * @param clause 综测条款
     * @return 结果
     */
    public int insertClause(AssessRegulationClause clause);

    /**
     * 修改综测条款
     *
     * @param clause 综测条款
     * @return 结果
     */
    public int updateClause(AssessRegulationClause clause);

    /**
     * 批量删除综测条款
     *
     * @param clauseIds 条款ID数组
     * @return 结果
     */
    public int deleteClauseByIds(Long[] clauseIds);
}
