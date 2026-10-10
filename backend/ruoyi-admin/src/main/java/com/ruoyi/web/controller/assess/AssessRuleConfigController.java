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
import com.ruoyi.assess.domain.AssessRuleConfig;
import com.ruoyi.assess.service.IAssessRuleConfigService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 综测规则引擎配置 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/assess/rule")
public class AssessRuleConfigController extends BaseController
{
    @Autowired
    private IAssessRuleConfigService ruleConfigService;

    /**
     * 查询规则配置列表
     */
    @PreAuthorize("@ss.hasPermi('assess:rule:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssessRuleConfig ruleConfig)
    {
        startPage();
        List<AssessRuleConfig> list = ruleConfigService.selectRuleConfigList(ruleConfig);
        return getDataTable(list);
    }

    /**
     * 导出规则配置列表
     */
    @PreAuthorize("@ss.hasPermi('assess:rule:export')")
    @Log(title = "规则引擎配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(AssessRuleConfig ruleConfig)
    {
        List<AssessRuleConfig> list = ruleConfigService.selectRuleConfigList(ruleConfig);
        ExcelUtil<AssessRuleConfig> util = new ExcelUtil<AssessRuleConfig>(AssessRuleConfig.class);
        return util.exportExcel(list, "规则引擎配置数据");
    }

    /**
     * 获取规则配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('assess:rule:query')")
    @GetMapping(value = "/{ruleId}")
    public AjaxResult getInfo(@PathVariable("ruleId") Long ruleId)
    {
        return AjaxResult.success(ruleConfigService.selectRuleConfigById(ruleId));
    }

    /**
     * 新增规则配置
     */
    @PreAuthorize("@ss.hasPermi('assess:rule:add')")
    @Log(title = "规则引擎配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AssessRuleConfig ruleConfig)
    {
        ruleConfig.setCreateBy(SecurityUtils.getUsername());
        return toAjax(ruleConfigService.insertRuleConfig(ruleConfig));
    }

    /**
     * 修改规则配置
     */
    @PreAuthorize("@ss.hasPermi('assess:rule:edit')")
    @Log(title = "规则引擎配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AssessRuleConfig ruleConfig)
    {
        ruleConfig.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(ruleConfigService.updateRuleConfig(ruleConfig));
    }

    /**
     * 删除规则配置
     */
    @PreAuthorize("@ss.hasPermi('assess:rule:remove')")
    @Log(title = "规则引擎配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ruleIds}")
    public AjaxResult remove(@PathVariable Long[] ruleIds)
    {
        return toAjax(ruleConfigService.deleteRuleConfigByIds(ruleIds));
    }
}
