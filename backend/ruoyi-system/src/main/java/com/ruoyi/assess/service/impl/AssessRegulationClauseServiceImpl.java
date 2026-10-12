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
import com.ruoyi.assess.ai.util.FileTextExtractor;
import com.ruoyi.assess.domain.AssessRegulationClause;
import com.ruoyi.assess.mapper.AssessRegulationClauseMapper;
import com.ruoyi.assess.service.IAssessRegulationClauseService;
import com.ruoyi.common.utils.StringUtils;

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
     * 导入条款
     * <p>
     * 流程: 文件抽文本 → AI识别条款 → 批量插入数据库
     */
    @Override
    public int importClauses(Long regulationId, MultipartFile file, String username)
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
            // 不支持的格式
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

        // 3. 调用AI提取条款
        List<AssessRegulationClause> clauses = clauseExtractService.extractClauses(text);
        if (clauses == null || clauses.isEmpty())
        {
            throw new RuntimeException("条款识别失败，请检查文件格式或手动录入");
        }

        // 4. 批量插入数据库
        int successCount = 0;
        int failCount = 0;
        int sortOrder = 1;

        for (AssessRegulationClause clause : clauses)
        {
            try
            {
                clause.setRegulationId(regulationId);
                clause.setParentId(0L);
                clause.setSortOrder(sortOrder++);
                clause.setCreateBy(username);
                clauseMapper.insertClause(clause);
                successCount++;
            }
            catch (Exception e)
            {
                failCount++;
                log.error("插入条款失败: clauseNo={}, clauseTitle={}",
                        clause.getClauseNo(), clause.getClauseTitle(), e);
            }
        }

        log.info("条款导入完成: 细则ID={}, 成功{}条, 失败{}条", regulationId, successCount, failCount);

        if (successCount == 0)
        {
            throw new RuntimeException("条款识别失败，请检查文件格式或手动录入");
        }

        return successCount;
    }
}
