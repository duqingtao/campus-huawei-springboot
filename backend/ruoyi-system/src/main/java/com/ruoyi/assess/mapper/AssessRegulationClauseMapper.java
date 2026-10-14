package com.ruoyi.assess.mapper;

import java.util.List;
import com.ruoyi.assess.domain.AssessRegulationClause;

/**
 * 综测条款 数据层
 *
 * @author ruoyi
 */
public interface AssessRegulationClauseMapper
{
    /**
     * 查询综测条款列表
     *
     * @param clause 综测条款
     * @return 综测条款集合
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
     * 根据细则ID查询所有条款
     *
     * @param regulationId 细则ID
     * @return 条款集合
     */
    public List<AssessRegulationClause> selectClauseByRegulationId(Long regulationId);

    /**
     * 根据细则ID查询叶子条款(仅clause_type=1且parent_id<>0，用于AI匹配候选)
     *
     * @param regulationId 细则ID
     * @return 叶子条款集合
     */
    public List<AssessRegulationClause> selectLeafClauseByRegulationId(Long regulationId);

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
     * 逻辑删除综测条款
     *
     * @param clauseId 条款ID
     * @return 结果
     */
    public int deleteClauseById(Long clauseId);

    /**
     * 批量逻辑删除综测条款
     *
     * @param clauseIds 条款ID数组
     * @return 结果
     */
    public int deleteClauseByIds(Long[] clauseIds);

    /**
     * 根据细则ID逻辑删除其下所有条款
     *
     * @param regulationId 细则ID
     * @return 结果
     */
    public int deleteClauseByRegulationId(Long regulationId);
}
