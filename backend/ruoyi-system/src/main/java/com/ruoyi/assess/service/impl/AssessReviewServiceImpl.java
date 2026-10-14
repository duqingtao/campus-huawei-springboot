package com.ruoyi.assess.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.assess.ai.ReviewCommentService;
import com.ruoyi.assess.ai.dto.DraftCommentRequest;
import com.ruoyi.assess.domain.AssessDeclaration;
import com.ruoyi.assess.domain.AssessReview;
import com.ruoyi.assess.domain.AssessReviewAuditDTO;
import com.ruoyi.assess.mapper.AssessDeclarationMapper;
import com.ruoyi.assess.mapper.AssessReviewMapper;
import com.ruoyi.assess.service.IAssessReviewService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 综测审核管理 服务层实现
 *
 * @author ruoyi
 */
@Service
public class AssessReviewServiceImpl implements IAssessReviewService
{
    private static final Logger log = LoggerFactory.getLogger(AssessReviewServiceImpl.class);

    @Autowired
    private AssessDeclarationMapper declarationMapper;

    @Autowired
    private AssessReviewMapper reviewMapper;

    @Autowired
    private ReviewCommentService reviewCommentService;

    /**
     * 待审看板列表：分页查 status=1(已提交) 的申报单
     */
    @Override
    public List<AssessDeclaration> selectReviewList(AssessDeclaration declaration)
    {
        // 强制只查已提交状态
        declaration.setStatus("1");
        return declarationMapper.selectDeclarationList(declaration);
    }

    /**
     * 审核操作
     * <p>
     * ① 校验申报单 status=1（已提交才能审）
     * ② 更新申报单 status + review_color + review_user_id + review_time + review_comment
     * ③ 插入 assess_review 审核记录
     */
    @Override
    @Transactional
    public int auditDeclaration(AssessReviewAuditDTO auditDTO, Long userId, String username, String nickName)
    {
        // ── 入参校验 ──
        if (auditDTO.getDeclarationId() == null)
        {
            throw new ServiceException("申报单ID不能为空");
        }
        String result = auditDTO.getResult();
        if (StringUtils.isEmpty(result) || !"pass".equals(result) && !"suspect".equals(result) && !"reject".equals(result))
        {
            throw new ServiceException("审核结果不合法，仅支持：pass=通过 suspect=存疑 reject=驳回");
        }
        // status + reviewColor 由服务端按 result 派生，不再要求前端传参
        // pass→status=4(全部通过),reviewColor=1(绿)
        // suspect→status=3(部分通过),reviewColor=2(黄)
        // reject→status=5(已驳回),reviewColor=3(红)
        String status;
        String reviewColor;
        if ("pass".equals(result))
        {
            status = "4";
            reviewColor = "1"; // 绿
        }
        else if ("suspect".equals(result))
        {
            status = "3";
            reviewColor = "2"; // 黄
        }
        else
        {
            status = "5";
            reviewColor = "3"; // 红
        }

        // ── ① 校验申报单状态 ──
        AssessDeclaration declaration = declarationMapper.selectDeclarationById(auditDTO.getDeclarationId());
        if (declaration == null)
        {
            throw new ServiceException("申报单不存在或已被删除");
        }
        if (!"1".equals(declaration.getStatus()))
        {
            throw new ServiceException("仅已提交状态的申报单可以审核，当前状态：" + declaration.getStatus());
        }

        // ── ② 更新申报单 ──
        AssessDeclaration update = new AssessDeclaration();
        update.setDeclarationId(auditDTO.getDeclarationId());
        update.setStatus(status);
        update.setReviewColor(reviewColor);
        update.setReviewUserId(userId);
        update.setReviewTime(new Date());
        update.setReviewComment(auditDTO.getReviewComment());
        update.setUpdateBy(username);
        declarationMapper.updateDeclaration(update);

        // ── ③ 插入审核记录 ──
        // review_round = 现有审核记录数 + 1
        int existingCount = reviewMapper.countByDeclarationId(auditDTO.getDeclarationId());

        // review_result 映射：pass/suspect → '1'(通过)，reject → '2'(驳回)
        String reviewResult = "reject".equals(result) ? "2" : "1";

        AssessReview review = new AssessReview();
        review.setDeclarationId(auditDTO.getDeclarationId());
        review.setReviewerId(userId);
        review.setReviewerName(nickName);
        review.setReviewRound(existingCount + 1);
        review.setReviewResult(reviewResult);
        review.setReviewComment(auditDTO.getReviewComment());
        review.setReviewTime(new Date());
        review.setCreateBy(username);
        reviewMapper.insertReview(review);

        log.info("申报单[{}]审核完成，审核人={}，结果={}，status={}，颜色={}，轮次={}",
            auditDTO.getDeclarationId(), username, result, status, reviewColor, existingCount + 1);
        return 1;
    }

    /**
     * 审核记录查询：按 declarationId 查历史审核记录列表
     */
    @Override
    public List<AssessReview> selectReviewHistory(Long declarationId)
    {
        return reviewMapper.selectReviewByDeclarationId(declarationId);
    }

    /**
     * 我的审核统计：当前审核人通过/存疑/驳回的数量统计
     */
    @Override
    public Map<String, Object> selectReviewStats(Long reviewUserId)
    {
        List<Map<String, Object>> rawStats = declarationMapper.selectReviewStats(reviewUserId);

        Map<String, Object> stats = new HashMap<>();
        int passedCount = 0;
        int suspiciousCount = 0;
        int rejectedCount = 0;

        if (rawStats != null)
        {
            for (Map<String, Object> row : rawStats)
            {
                String color = String.valueOf(row.get("reviewColor"));
                int count = ((Number) row.get("count")).intValue();
                if ("1".equals(color))
                {
                    passedCount = count;
                }
                else if ("2".equals(color))
                {
                    suspiciousCount = count;
                }
                else if ("3".equals(color))
                {
                    rejectedCount = count;
                }
            }
        }

        stats.put("passedCount", passedCount);
        stats.put("suspiciousCount", suspiciousCount);
        stats.put("rejectedCount", rejectedCount);
        stats.put("totalCount", passedCount + suspiciousCount + rejectedCount);
        return stats;
    }

    /**
     * AI驳回意见草稿（Mock阶段）
     */
    @Override
    public String draftComment(Long declarationId)
    {
        DraftCommentRequest request = new DraftCommentRequest();
        request.setDeclarationId(declarationId);
        return reviewCommentService.generateDraftComment(request);
    }
}
