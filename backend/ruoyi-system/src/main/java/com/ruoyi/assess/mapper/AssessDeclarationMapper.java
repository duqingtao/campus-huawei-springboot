package com.ruoyi.assess.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.assess.domain.AssessDeclaration;

/**
 * 综测申报单 数据层
 *
 * @author ruoyi
 */
public interface AssessDeclarationMapper
{
    /**
     * 查询综测申报单列表
     */
    public List<AssessDeclaration> selectDeclarationList(AssessDeclaration declaration);

    /**
     * 通过申报单ID查询申报单信息
     */
    public AssessDeclaration selectDeclarationById(Long declarationId);

    /**
     * 新增综测申报单
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
     * 查询当天最大申报单号(用于序号生成)
     *
     * @param prefix 前缀，如 'SB20251010%'
     * @return 最大单号
     */
    public String selectMaxDeclarationNoToday(String prefix);

    /**
     * 审核统计：按review_color分组统计某审核人审过的申报单数量
     *
     * @param reviewUserId 审核人用户ID
     * @return List of Map: {reviewColor, count}
     */
    public List<Map<String, Object>> selectReviewStats(Long reviewUserId);
}
