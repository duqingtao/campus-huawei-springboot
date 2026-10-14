package com.ruoyi.web.controller.assess;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.assess.ai.dto.ClausePreviewResult;
import com.ruoyi.assess.domain.AssessRegulationClause;
import com.ruoyi.assess.service.IAssessRegulationClauseService;

/**
 * 综测条款管理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/assess/clause")
public class AssessRegulationClauseController extends BaseController
{
    @Autowired
    private IAssessRegulationClauseService clauseService;

    /**
     * 查询综测条款列表
     */
    @PreAuthorize("@ss.hasPermi('assess:clause:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssessRegulationClause clause)
    {
        // 条款以树形展示，不分页
        List<AssessRegulationClause> list = clauseService.selectClauseTreeList(clause);
        return getDataTable(list);
    }

    /**
     * 导出综测条款列表
     */
    @PreAuthorize("@ss.hasPermi('assess:clause:export')")
    @Log(title = "条款管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssessRegulationClause clause)
    {
        List<AssessRegulationClause> list = clauseService.selectClauseList(clause);
        ExcelUtil<AssessRegulationClause> util = new ExcelUtil<AssessRegulationClause>(AssessRegulationClause.class);
        util.exportExcel(response, list, "条款数据");
    }

    /**
     * 根据条款ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('assess:clause:query')")
    @GetMapping(value = "/{clauseId}")
    public AjaxResult getInfo(@PathVariable Long clauseId)
    {
        return success(clauseService.selectClauseById(clauseId));
    }

    /**
     * 根据细则ID查询条款树形结构
     */
    @PreAuthorize("@ss.hasPermi('assess:clause:query')")
    @GetMapping("/tree/{regulationId}")
    public AjaxResult tree(@PathVariable Long regulationId)
    {
        List<AssessRegulationClause> tree = clauseService.selectClauseTreeByRegulationId(regulationId);
        return success(tree);
    }

    /**
     * 新增综测条款
     */
    @PreAuthorize("@ss.hasPermi('assess:clause:add')")
    @Log(title = "条款管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AssessRegulationClause clause)
    {
        clause.setCreateBy(getUsername());
        return toAjax(clauseService.insertClause(clause));
    }

    /**
     * 修改综测条款
     */
    @PreAuthorize("@ss.hasPermi('assess:clause:edit')")
    @Log(title = "条款管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AssessRegulationClause clause)
    {
        clause.setUpdateBy(getUsername());
        return toAjax(clauseService.updateClause(clause));
    }

    /**
     * 删除综测条款
     */
    @PreAuthorize("@ss.hasPermi('assess:clause:remove')")
    @Log(title = "条款管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{clauseIds}")
    public AjaxResult remove(@PathVariable Long[] clauseIds)
    {
        return toAjax(clauseService.deleteClauseByIds(clauseIds));
    }

    /**
     * 预览导入条款
     * <p>
     * 上传综测细则文件(PDF/Word/TXT)，AI提取条款后返回预览结果，不落库。
     *
     * @param regulationId 所属细则ID
     * @param file         上传的文件
     * @return 预览结果，含条款模块树和元数据
     */
    @PreAuthorize("@ss.hasPermi('assess:clause:add')")
    @Log(title = "条款导入", businessType = BusinessType.IMPORT)
    @PostMapping("/import/preview")
    public AjaxResult previewImport(@RequestParam("regulationId") Long regulationId,
            @RequestParam("file") MultipartFile file)
    {
        try
        {
            ClausePreviewResult result = clauseService.previewImportClauses(regulationId, file);
            return AjaxResult.success(result);
        }
        catch (IllegalArgumentException e)
        {
            return AjaxResult.error(e.getMessage());
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 确认导入条款
     * <p>
     * 将前端确认后的条款树批量插入数据库。
     *
     * @param regulationId 所属细则ID
     * @param clauses      确认后的条款模块树
     * @return 导入结果，含成功条数
     */
    @PreAuthorize("@ss.hasPermi('assess:clause:add')")
    @Log(title = "条款导入", businessType = BusinessType.IMPORT)
    @PostMapping("/import/confirm")
    public AjaxResult confirmImport(@RequestParam("regulationId") Long regulationId,
            @RequestBody List<AssessRegulationClause> clauses)
    {
        try
        {
            int count = clauseService.confirmImportClauses(regulationId, clauses, getUsername());
            return AjaxResult.success("成功导入" + count + "条条款", count);
        }
        catch (IllegalArgumentException e)
        {
            return AjaxResult.error(e.getMessage());
        }
        catch (Exception e)
        {
            return AjaxResult.error("条款导入失败：" + e.getMessage());
        }
    }
}
