package com.ruoyi.assess.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.assess.ai.dto.ClausePreviewResult;
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
     */
    public List<AssessRegulationClause> selectClauseList(AssessRegulationClause clause);

    /**
     * 查询综测条款列表（树形结构）
     */
    public List<AssessRegulationClause> selectClauseTreeList(AssessRegulationClause clause);

    /**
     * 通过条款ID查询条款信息
     */
    public AssessRegulationClause selectClauseById(Long clauseId);

    /**
     * 根据细则ID查询条款树形结构
     */
    public List<AssessRegulationClause> selectClauseTreeByRegulationId(Long regulationId);

    /**
     * 新增综测条款
     */
    public int insertClause(AssessRegulationClause clause);

    /**
     * 修改综测条款
     */
    public int updateClause(AssessRegulationClause clause);

    /**
     * 批量删除综测条款
     */
    public int deleteClauseByIds(Long[] clauseIds);

    /**
     * 预览导入条款
     * <p>
     * 从文件中抽取文本，调用AI提取条款，返回预览结果（不落库）。
     *
     * @param regulationId 所属细则ID
     * @param file         上传的文件(PDF/Word/TXT)
     * @return 预览结果，含条款模块树和元数据
     */
    public ClausePreviewResult previewImportClauses(Long regulationId, MultipartFile file);

    /**
     * 确认导入条款
     * <p>
     * 将前端确认后的条款树批量插入数据库。
     *
     * @param regulationId 所属细则ID
     * @param clauses      确认后的条款模块树
     * @param username     操作人用户名
     * @return 成功导入的条款数量
     */
    public int confirmImportClauses(Long regulationId, List<AssessRegulationClause> clauses, String username);
}
