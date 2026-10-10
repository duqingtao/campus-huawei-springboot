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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.assess.domain.AssessRegulation;
import com.ruoyi.assess.service.IAssessRegulationService;

/**
 * 综测细则管理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/assess/regulation")
public class AssessRegulationController extends BaseController
{
    @Autowired
    private IAssessRegulationService regulationService;

    /**
     * 查询综测细则列表
     */
    @PreAuthorize("@ss.hasPermi('assess:regulation:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssessRegulation regulation)
    {
        startPage();
        List<AssessRegulation> list = regulationService.selectRegulationList(regulation);
        return getDataTable(list);
    }

    /**
     * 导出综测细则列表
     */
    @PreAuthorize("@ss.hasPermi('assess:regulation:export')")
    @Log(title = "细则管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssessRegulation regulation)
    {
        List<AssessRegulation> list = regulationService.selectRegulationList(regulation);
        ExcelUtil<AssessRegulation> util = new ExcelUtil<AssessRegulation>(AssessRegulation.class);
        util.exportExcel(response, list, "细则数据");
    }

    /**
     * 根据细则ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('assess:regulation:query')")
    @GetMapping(value = "/{regulationId}")
    public AjaxResult getInfo(@PathVariable Long regulationId)
    {
        return success(regulationService.selectRegulationById(regulationId));
    }

    /**
     * 新增综测细则
     */
    @PreAuthorize("@ss.hasPermi('assess:regulation:add')")
    @Log(title = "细则管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AssessRegulation regulation)
    {
        if (!regulationService.checkRegulationNoUnique(regulation))
        {
            return error("新增细则'" + regulation.getRegulationNo() + "'失败，细则编号已存在");
        }
        regulation.setCreateBy(getUsername());
        return toAjax(regulationService.insertRegulation(regulation));
    }

    /**
     * 修改综测细则
     */
    @PreAuthorize("@ss.hasPermi('assess:regulation:edit')")
    @Log(title = "细则管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AssessRegulation regulation)
    {
        if (!regulationService.checkRegulationNoUnique(regulation))
        {
            return error("修改细则'" + regulation.getRegulationNo() + "'失败，细则编号已存在");
        }
        regulation.setUpdateBy(getUsername());
        return toAjax(regulationService.updateRegulation(regulation));
    }

    /**
     * 删除综测细则
     */
    @PreAuthorize("@ss.hasPermi('assess:regulation:remove')")
    @Log(title = "细则管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{regulationIds}")
    public AjaxResult remove(@PathVariable Long[] regulationIds)
    {
        return toAjax(regulationService.deleteRegulationByIds(regulationIds));
    }
}
