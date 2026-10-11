package com.ruoyi.assess.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.assess.domain.AssessDeclaration;
import com.ruoyi.assess.domain.AssessReview;
import com.ruoyi.assess.domain.AssessReviewAuditDTO;

/**
 * 综测审核管理 服务层
 *
 * @author ruoyi
 */
public interface IAssessReviewService
{
    /**
     * 待审看板列表：分页查 status=1(已提交) 的申报单
     */
    public List<AssessDeclaration> selectReviewList(AssessDeclaration declaration);

    /**
     * 审核操作（@Transactional）
     * <p>
     * ① 校验申报单 status=1
     * ② 更新申报单 status + review_color + review_user_id + review_time + review_comment
     * ③ 插入 assess_review 审核记录
     */
    public int auditDeclaration(AssessReviewAuditDTO auditDTO, Long userId, String username, String nickName);

    /**
     * 审核记录查询：按 declarationId 查历史审核记录列表
     */
    public List<AssessReview> selectReviewHistory(Long declarationId);

    /**
     * 我的审核统计：当前审核人通过/存疑/驳回的数量统计
     *
     * @param reviewUserId 审核人用户ID
     * @return Map: passedCount / suspiciousCount / rejectedCount / totalCount
     */
    public Map<String, Object> selectReviewStats(Long reviewUserId);

    /**
     * AI驳回意见草稿（Mock阶段）
     */
    public String draftComment(Long declarationId);
}
