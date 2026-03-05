package com.ruoyi.process.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.process.domain.BizSopStep;
import com.ruoyi.process.service.IBizSopStepService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * SOP步骤配置Controller
 * 
 * @author ruoyi
 * @date 2025-12-23
 */
@RestController
@RequestMapping("/process/step")
public class BizSopStepController extends BaseController
{
    @Autowired
    private IBizSopStepService bizSopStepService;

    /**
     * 查询SOP步骤配置列表
     */
    @PreAuthorize("@ss.hasPermi('process:step:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizSopStep bizSopStep)
    {
        startPage();
        List<BizSopStep> list = bizSopStepService.selectBizSopStepList(bizSopStep);
        return getDataTable(list);
    }

    /**
     * 导出SOP步骤配置列表
     */
    @PreAuthorize("@ss.hasPermi('process:step:export')")
    @Log(title = "SOP步骤配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizSopStep bizSopStep)
    {
        List<BizSopStep> list = bizSopStepService.selectBizSopStepList(bizSopStep);
        ExcelUtil<BizSopStep> util = new ExcelUtil<BizSopStep>(BizSopStep.class);
        util.exportExcel(response, list, "SOP步骤配置数据");
    }

    /**
     * 获取SOP步骤配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('process:step:query')")
    @GetMapping(value = "/{stepId}")
    public AjaxResult getInfo(@PathVariable("stepId") Long stepId)
    {
        return success(bizSopStepService.selectBizSopStepByStepId(stepId));
    }

    /**
     * 新增SOP步骤配置
     */
    @PreAuthorize("@ss.hasPermi('process:step:add')")
    @Log(title = "SOP步骤配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizSopStep bizSopStep)
    {
        return toAjax(bizSopStepService.insertBizSopStep(bizSopStep));
    }

    /**
     * 修改SOP步骤配置
     */
    @PreAuthorize("@ss.hasPermi('process:step:edit')")
    @Log(title = "SOP步骤配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizSopStep bizSopStep)
    {
        return toAjax(bizSopStepService.updateBizSopStep(bizSopStep));
    }

    /**
     * 删除SOP步骤配置
     */
    @PreAuthorize("@ss.hasPermi('process:step:remove')")
    @Log(title = "SOP步骤配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{stepIds}")
    public AjaxResult remove(@PathVariable Long[] stepIds)
    {
        return toAjax(bizSopStepService.deleteBizSopStepByStepIds(stepIds));
    }
}
