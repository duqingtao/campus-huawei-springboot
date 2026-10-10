package com.ruoyi.assess.service;

import java.util.List;
import com.ruoyi.assess.domain.AssessDeclaration;

/**
 * 综测申报单 服务层
 *
 * @author ruoyi
 */
public interface IAssessDeclarationService
{
    /**
     * 查询综测申报单列表（含数据隔离）
     */
    public List<AssessDeclaration> selectDeclarationList(AssessDeclaration declaration);

    /**
     * 通过申报单ID查询申报单信息（含明细列表）
     */
    public AssessDeclaration selectDeclarationById(Long declarationId);

    /**
     * 新增综测申报单（含自动生成单号、创建明细）
     */
    public int insertDeclaration(AssessDeclaration declaration);

    /**
     * 修改综测申报单
     */
    public int updateDeclaration(AssessDeclaration declaration);

    /**
     * 批量逻辑删除综测申报单
     */
    public int deleteDeclarationByIds(Long[] declarationIds);

    /**
     * 提交申报单（核心流程：AI匹配→规则引擎→定分→汇总）
     */
    public int submitDeclaration(Long declarationId, String username);
}
