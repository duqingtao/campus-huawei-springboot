package com.ruoyi.web.controller.assess;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.assess.domain.AssessDeclaration;
import com.ruoyi.assess.domain.AssessReview;
import com.ruoyi.assess.domain.AssessReviewAuditDTO;
import com.ruoyi.assess.service.IAssessReviewService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 综测审核管理 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/assess/review")
public class AssessReviewController extends BaseController
{
    @Autowired
    private IAssessReviewService reviewService;

    /**
     * 待审看板列表：分页查 status=1(已提交) 的申报单
     */
    @PreAuthorize("@ss.hasPermi('assess:review:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssessDeclaration declaration)
    {
        startPage();
        List<AssessDeclaration> list = reviewService.selectReviewList(declaration);
        return getDataTable(list);
    }

    /**
     * 审核操作
     * <p>
     * 入参：declarationId + result(3部分通过/4全部通过/5已驳回) + reviewColor(1绿2黄3红) + reviewComment
     */
    @PreAuthorize("@ss.hasPermi('assess:review:edit')")
    @Log(title = "综测审核操作", businessType = BusinessType.UPDATE)
    @PostMapping("/audit")
    public AjaxResult audit(@RequestBody AssessReviewAuditDTO auditDTO)
    {
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();
        String nickName = SecurityUtils.getLoginUser().getUser().getNickName();
        return toAjax(reviewService.auditDeclaration(auditDTO, userId, username, nickName));
    }

    /**
     * 审核记录查询：按 declarationId 查历史审核记录列表
     */
    @PreAuthorize("@ss.hasPermi('assess:review:query')")
    @GetMapping("/history/{declarationId}")
    public AjaxResult history(@PathVariable("declarationId") Long declarationId)
    {
        List<AssessReview> list = reviewService.selectReviewHistory(declarationId);
        return AjaxResult.success(list);
    }

    /**
     * 我的审核统计：当前辅导员审核通过/存疑/驳回的数量统计
     */
    @PreAuthorize("@ss.hasPermi('assess:review:query')")
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(reviewService.selectReviewStats(userId));
    }

    /**
     * AI驳回意见草稿（Mock阶段）
     * <p>
     * 权限复用审核操作权限 assess:review:edit
     */
    @PreAuthorize("@ss.hasPermi('assess:review:edit')")
    @Log(title = "生成驳回意见草稿", businessType = BusinessType.OTHER)
    @PostMapping("/draftComment")
    public AjaxResult draftComment(@RequestBody AssessReviewAuditDTO draftDTO)
    {
        String draft = reviewService.draftComment(draftDTO.getDeclarationId());
        return AjaxResult.success("操作成功", draft);




    }
}
