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
import com.ruoyi.process.domain.BizDeviceModel;
import com.ruoyi.process.service.IBizDeviceModelService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/process/model")
public class BizDeviceModelController extends BaseController
{
    @Autowired
    private IBizDeviceModelService bizDeviceModelService;

    /**
     * 查询转辙机型号列表
     */
    @PreAuthorize("@ss.hasPermi('process:model:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizDeviceModel bizDeviceModel)
    {
        startPage();
        List<BizDeviceModel> list = bizDeviceModelService.selectBizDeviceModelList(bizDeviceModel);
        return getDataTable(list);
    }

    /**
     * 导出转辙机型号列表
     */
    @PreAuthorize("@ss.hasPermi('process:model:export')")
    @Log(title = "转辙机型号", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizDeviceModel bizDeviceModel)
    {
        List<BizDeviceModel> list = bizDeviceModelService.selectBizDeviceModelList(bizDeviceModel);
        ExcelUtil<BizDeviceModel> util = new ExcelUtil<BizDeviceModel>(BizDeviceModel.class);
        util.exportExcel(response, list, "转辙机型号数据");
    }

    /**
     * 获取转辙机型号详细信息
     */
    @PreAuthorize("@ss.hasPermi('process:model:query')")
    @GetMapping(value = "/{modelId}")
    public AjaxResult getInfo(@PathVariable("modelId") Long modelId)
    {
        return success(bizDeviceModelService.selectBizDeviceModelByModelId(modelId));
    }

    /**
     * 新增转辙机型号
     */
    @PreAuthorize("@ss.hasPermi('process:model:add')")
    @Log(title = "转辙机型号", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizDeviceModel bizDeviceModel)
    {
        return toAjax(bizDeviceModelService.insertBizDeviceModel(bizDeviceModel));
    }

    /**
     * 修改转辙机型号
     */
    @PreAuthorize("@ss.hasPermi('process:model:edit')")
    @Log(title = "转辙机型号", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizDeviceModel bizDeviceModel)
    {
        return toAjax(bizDeviceModelService.updateBizDeviceModel(bizDeviceModel));
    }

    /**
     * 删除转辙机型号
     */
    @PreAuthorize("@ss.hasPermi('process:model:remove')")
    @Log(title = "转辙机型号", businessType = BusinessType.DELETE)
	@DeleteMapping("/{modelIds}")
    public AjaxResult remove(@PathVariable Long[] modelIds)
    {
        return toAjax(bizDeviceModelService.deleteBizDeviceModelByModelIds(modelIds));
    }
}
