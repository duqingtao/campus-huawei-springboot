package com.ruoyi.web.controller.assess;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.assess.domain.AssessStatsStudent;
import com.ruoyi.assess.service.IAssessStatsService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 班级统计 Controller（只读）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/assess/stats")
public class AssessStatsController extends BaseController
{
    @Autowired
    private IAssessStatsService statsService;

    /**
     * 班级概览：学生数、待审/审核中/通过/驳回申报单数、平均分
     */
    @PreAuthorize("@ss.hasPermi('assess:stats:query')")
    @GetMapping("/overview")
    public AjaxResult overview(@RequestParam(value = "deptId", required = false) Long deptId)
    {
        return AjaxResult.success(statsService.selectOverview(deptId));
    }

    /**
     * 本班学生申报明细列表（只读分页）
     */
    @PreAuthorize("@ss.hasPermi('assess:stats:query')")
    @GetMapping("/students")
    public TableDataInfo students(AssessStatsStudent query)
    {
        startPage();
        List<AssessStatsStudent> list = statsService.selectStudentList(query);
        return getDataTable(list);
    }

    /**
     * 本班申报汇总导出
     */
    @PreAuthorize("@ss.hasPermi('assess:stats:export')")
    @Log(title = "班级统计", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(AssessStatsStudent query)
    {
        List<AssessStatsStudent> list = statsService.selectStudentList(query);
        ExcelUtil<AssessStatsStudent> util = new ExcelUtil<AssessStatsStudent>(AssessStatsStudent.class);
        return util.exportExcel(list, "班级申报汇总");
    }
}
