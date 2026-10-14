package com.ruoyi.assess.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.assess.ai.ClauseExtractService;
import com.ruoyi.assess.ai.dto.ClausePreviewResult;
import com.ruoyi.assess.ai.util.FileTextExtractor;
import com.ruoyi.assess.domain.AssessRegulationClause;
import com.ruoyi.assess.mapper.AssessRegulationClauseMapper;
import com.ruoyi.assess.service.IAssessRegulationClauseService;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * 综测条款 服务层实现
 *
 * @author ruoyi
 */
@Service
public class AssessRegulationClauseServiceImpl implements IAssessRegulationClauseService
{
    private static final Logger log = LoggerFactory.getLogger(AssessRegulationClauseServiceImpl.class);

    /** 最大文件大小10MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Autowired
    private AssessRegulationClauseMapper clauseMapper;

    @Autowired
    private ClauseExtractService clauseExtractService;

    /**
     * 查询综测条款列表
     */
    @Override
    public List<AssessRegulationClause> selectClauseList(AssessRegulationClause clause)
    {
        return clauseMapper.selectClauseList(clause);
    }

    @Override
    public List<AssessRegulationClause> selectClauseTreeList(AssessRegulationClause clause)
    {
        List<AssessRegulationClause> allClauses = clauseMapper.selectClauseList(clause);
        return buildClauseTree(allClauses);
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

    /**
     * 预览导入条款
     * <p>
     * 流程: 文件抽文本 → AI提取条款(含分块) → 返回预览结果，不落库
     */
    @Override
    public ClausePreviewResult previewImportClauses(Long regulationId, MultipartFile file)
    {
        // 1. 参数校验
        if (regulationId == null)
        {
            throw new IllegalArgumentException("所属细则不能为空");
        }
        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE)
        {
            throw new IllegalArgumentException("文件超过10MB限制");
        }

        // 2. 文件文本抽取
        String text;
        try (InputStream is = file.getInputStream())
        {
            text = FileTextExtractor.extractText(is, file.getOriginalFilename());
        }
        catch (IllegalArgumentException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("文件文本抽取失败, fileName={}", file.getOriginalFilename(), e);
            throw new RuntimeException("文件文本抽取失败：" + e.getMessage(), e);
        }

        if (StringUtils.isEmpty(text) || text.trim().length() < 10)
        {
            throw new RuntimeException("文件内容为空或文本过少，无法识别条款");
        }

        // 3. 调用AI提取条款（含分块逻辑）
        ClausePreviewResult result = clauseExtractService.extractClausesWithMeta(text);

        // 4. 如果有明确错误（如无法分块），抛出让前端展示
        if (result.getMeta().getErrors() != null && !result.getMeta().getErrors().isEmpty())
        {
            String firstError = result.getMeta().getErrors().get(0);
            if (result.getClauses() == null || result.getClauses().isEmpty())
            {
                throw new RuntimeException(firstError);
            }
        }

        if (result.getClauses() == null || result.getClauses().isEmpty())
        {
            throw new RuntimeException("条款识别失败，未提取到任何条款，请检查文件内容或手动录入");
        }

        return result;
    }

    /**
     * 确认导入条款
     * <p>
     * 将前端确认后的条款树批量插入数据库。
     * 按数组顺序自动填 sort_order、regulation_id、create_by、parent_id。
     * 单次导入条数上限 200。
     */
    @Override
    @Transactional
    public int confirmImportClauses(Long regulationId, List<AssessRegulationClause> clauses, String username)
    {
        if (regulationId == null)
        {
            throw new IllegalArgumentException("所属细则不能为空");
        }
        if (clauses == null || clauses.isEmpty())
        {
            throw new IllegalArgumentException("条款列表不能为空");
        }

        // 统计总条数（含子项），上限200
        int totalCount = 0;
        for (AssessRegulationClause module : clauses)
        {
            totalCount++;
            if (module.getChildren() != null)
            {
                totalCount += module.getChildren().size();
            }
        }
        if (totalCount > 200)
        {
            throw new IllegalArgumentException("单次导入条数上限200，当前" + totalCount + "条，请减少后重试");
        }

        int successCount = 0;
        int sortOrder = 1;

        for (AssessRegulationClause module : clauses)
        {
            // 插入模块（顶级条款）
            module.setRegulationId(regulationId);
            module.setParentId(0L);
            module.setSortOrder(sortOrder++);
            module.setCreateBy(username);
            clauseMapper.insertClause(module);
            successCount++;

            // 插入子项
            if (module.getChildren() != null)
            {
                for (AssessRegulationClause child : module.getChildren())
                {
                    child.setRegulationId(regulationId);
                    child.setParentId(module.getClauseId());
                    child.setSortOrder(sortOrder++);
                    child.setCreateBy(username);
                    clauseMapper.insertClause(child);
                    successCount++;
                }
            }
        }

        log.info("条款确认导入完成: 细则ID={}, 成功{}条", regulationId, successCount);
        return successCount;
    }
}
