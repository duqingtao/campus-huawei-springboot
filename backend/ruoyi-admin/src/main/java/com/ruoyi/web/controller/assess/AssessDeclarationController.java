package com.ruoyi.web.controller.assess;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.assess.domain.AssessDeclaration;
import com.ruoyi.assess.service.IAssessDeclarationService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 综测申报单 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/assess/declaration")
public class AssessDeclarationController extends BaseController
{
    @Autowired
    private IAssessDeclarationService declarationService;

    /**
     * 查询综测申报单列表
     */
    @PreAuthorize("@ss.hasPermi('assess:declaration:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssessDeclaration declaration)
    {
        startPage();
        List<AssessDeclaration> list = declarationService.selectDeclarationList(declaration);
        return getDataTable(list);
    }

    /**
     * 导出综测申报单列表
     */
    @PreAuthorize("@ss.hasPermi('assess:declaration:export')")
    @Log(title = "综测申报单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(AssessDeclaration declaration)
    {
        List<AssessDeclaration> list = declarationService.selectDeclarationList(declaration);
        ExcelUtil<AssessDeclaration> util = new ExcelUtil<AssessDeclaration>(AssessDeclaration.class);
        return util.exportExcel(list, "综测申报单数据");
    }

    /**
     * 获取综测申报单详细信息（含明细列表）
     */
    @PreAuthorize("@ss.hasPermi('assess:declaration:query')")
    @GetMapping(value = "/{declarationId}")
    public AjaxResult getInfo(@PathVariable("declarationId") Long declarationId)
    {
        return AjaxResult.success(declarationService.selectDeclarationById(declarationId));
    }

    /**
     * 新增综测申报单（草稿）
     */
    @PreAuthorize("@ss.hasPermi('assess:declaration:add')")
    @Log(title = "综测申报单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AssessDeclaration declaration)
    {
        declaration.setCreateBy(SecurityUtils.getUsername());
        return toAjax(declarationService.insertDeclaration(declaration));
    }

    /**
     * 修改综测申报单（仅草稿状态）
     */
    @PreAuthorize("@ss.hasPermi('assess:declaration:edit')")
    @Log(title = "综测申报单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AssessDeclaration declaration)
    {
        declaration.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(declarationService.updateDeclaration(declaration));
    }

    /**
     * 删除综测申报单（仅草稿状态）
     */
    @PreAuthorize("@ss.hasPermi('assess:declaration:remove')")
    @Log(title = "综测申报单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{declarationIds}")
    public AjaxResult remove(@PathVariable Long[] declarationIds)
    {
        return toAjax(declarationService.deleteDeclarationByIds(declarationIds));
    }

    /**
     * 提交申报单 — 触发AI匹配+规则引擎+定分+汇总
     */
    @PreAuthorize("@ss.hasPermi('assess:declaration:edit')")
    @Log(title = "提交综测申报单", businessType = BusinessType.UPDATE)
    @PostMapping("/submit/{declarationId}")
    public AjaxResult submit(@PathVariable Long declarationId)
    {
        return toAjax(declarationService.submitDeclaration(declarationId, SecurityUtils.getUsername()));
    }
}
