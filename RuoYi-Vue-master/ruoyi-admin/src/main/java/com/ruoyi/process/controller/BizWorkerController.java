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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.process.domain.BizWorker;
import com.ruoyi.process.service.IBizWorkerService;

/**
 * 工人信息 Controller
 */
@RestController
@RequestMapping("/process/worker")
public class BizWorkerController extends BaseController
{
    @Autowired
    private IBizWorkerService bizWorkerService;

    /**
     * 查询工人信息列表
     */
    // @PreAuthorize("@ss.hasPermi('process:worker:list')") // 如果不需要权限控制，可以注释掉这行
    @GetMapping("/list")
    public TableDataInfo list(BizWorker bizWorker)
    {
        startPage();
        List<BizWorker> list = bizWorkerService.selectBizWorkerList(bizWorker);
        return getDataTable(list);
    }

    /**
     * 导出工人信息列表
     */
    // @PreAuthorize("@ss.hasPermi('process:worker:export')")
    @Log(title = "工人信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizWorker bizWorker)
    {
        List<BizWorker> list = bizWorkerService.selectBizWorkerList(bizWorker);
        ExcelUtil<BizWorker> util = new ExcelUtil<BizWorker>(BizWorker.class);
        util.exportExcel(response, list, "工人信息数据");
    }

    /**
     * 获取工人信息详细信息
     */
    // @PreAuthorize("@ss.hasPermi('process:worker:query')")
    @GetMapping(value = "/{workerId}")
    public AjaxResult getInfo(@PathVariable("workerId") Long workerId)
    {
        return AjaxResult.success(bizWorkerService.selectBizWorkerById(workerId));
    }

    /**
     * 新增工人信息
     */
    // @PreAuthorize("@ss.hasPermi('process:worker:add')")
    @Log(title = "工人信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizWorker bizWorker)
    {
        return toAjax(bizWorkerService.insertBizWorker(bizWorker));
    }

    /**
     * 修改工人信息
     */
    // @PreAuthorize("@ss.hasPermi('process:worker:edit')")
    @Log(title = "工人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizWorker bizWorker)
    {
        return toAjax(bizWorkerService.updateBizWorker(bizWorker));
    }

    /**
     * 删除工人信息
     */
    // @PreAuthorize("@ss.hasPermi('process:worker:remove')")
    @Log(title = "工人信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{workerIds}")
    public AjaxResult remove(@PathVariable Long[] workerIds)
    {
        return toAjax(bizWorkerService.deleteBizWorkerByIds(workerIds));
    }
}