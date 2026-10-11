package com.ruoyi.assess.mapper;

import java.util.List;
import com.ruoyi.assess.domain.AssessReview;

/**
 * 综测审核记录 数据层
 *
 * @author ruoyi
 */
public interface AssessReviewMapper
{
    /**
     * 按申报单ID查询审核记录列表(按审核时间倒序)
     */
    public List<AssessReview> selectReviewByDeclarationId(Long declarationId);

    /**
     * 新增审核记录
     */
    public int insertReview(AssessReview review);

    /**
     * 统计某申报单的审核记录数(用于计算review_round)
     */
    public int countByDeclarationId(Long declarationId);
}
