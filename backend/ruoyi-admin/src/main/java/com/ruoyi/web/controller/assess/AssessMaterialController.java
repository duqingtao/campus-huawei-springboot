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
import com.ruoyi.assess.domain.AssessMaterial;
import com.ruoyi.assess.service.IAssessMaterialService;

/**
 * 综测材料管理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/assess/material")
public class AssessMaterialController extends BaseController
{
    @Autowired
    private IAssessMaterialService materialService;

    /**
     * 查询综测材料列表
     */
    @PreAuthorize("@ss.hasPermi('assess:material:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssessMaterial material)
    {
        startPage();
        List<AssessMaterial> list = materialService.selectMaterialList(material);
        return getDataTable(list);
    }

    /**
     * 导出综测材料列表
     */
    @PreAuthorize("@ss.hasPermi('assess:material:export')")
    @Log(title = "材料管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssessMaterial material)
    {
        List<AssessMaterial> list = materialService.selectMaterialList(material);
        ExcelUtil<AssessMaterial> util = new ExcelUtil<AssessMaterial>(AssessMaterial.class);
        util.exportExcel(response, list, "材料数据");
    }

    /**
     * 根据材料ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('assess:material:query')")
    @GetMapping(value = "/{materialId}")
    public AjaxResult getInfo(@PathVariable Long materialId)
    {
        return success(materialService.selectMaterialById(materialId));
    }

    /**
     * 上传登记新增综测材料
     */
    @PreAuthorize("@ss.hasPermi('assess:material:upload')")
    @Log(title = "材料管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestParam("file") MultipartFile file,
            @Validated AssessMaterial material)
    {
        return toAjax(materialService.insertMaterial(file, material, getUsername()));
    }

    /**
     * 修改综测材料
     */
    @PreAuthorize("@ss.hasPermi('assess:material:edit')")
    @Log(title = "材料管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AssessMaterial material)
    {
        material.setUpdateBy(getUsername());
        return toAjax(materialService.updateMaterial(material));
    }

    /**
     * 删除综测材料
     */
    @PreAuthorize("@ss.hasPermi('assess:material:remove')")
    @Log(title = "材料管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{materialIds}")
    public AjaxResult remove(@PathVariable Long[] materialIds)
    {
        return toAjax(materialService.deleteMaterialByIds(materialIds));
    }

    /**
     * 重新识别：重置OCR状态和抽取状态
     */
    @PreAuthorize("@ss.hasPermi('assess:material:reprocess')")
    @Log(title = "材料管理", businessType = BusinessType.UPDATE)
    @PostMapping("/reprocess/{materialId}")
    public AjaxResult reprocess(@PathVariable Long materialId)
    {
        return toAjax(materialService.reprocessMaterial(materialId, getUsername()));
    }
}
