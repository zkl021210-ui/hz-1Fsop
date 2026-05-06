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
import com.ruoyi.process.domain.BizAssemblyTask;
import com.ruoyi.process.service.IBizAssemblyTaskService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.StringUtils;

/**
 * 装配任务主Controller
 */
@RestController
@RequestMapping("/process/task")
public class BizAssemblyTaskController extends BaseController
{
    @Autowired
    private IBizAssemblyTaskService bizAssemblyTaskService;

    /**
     * 开始或恢复一个装配任务 (扫码开工)
     */
    @PostMapping("/start")
    public AjaxResult startTask(@RequestBody BizAssemblyTask taskParams)
    {
        BizAssemblyTask task = bizAssemblyTaskService.startTask(taskParams);
        return AjaxResult.success("操作成功", task);
    }

    @PostMapping("/aiNextStep")
    public AjaxResult aiNextStep(@RequestBody java.util.Map<String, String> payload) {
        String deviceSn = payload.get("deviceSn");
        String action = payload.get("action");

        System.out.println(" [Java] 收到 AI 通知: 设备=" + deviceSn + ", 动作=" + action);

        if ("AUTO_NEXT".equals(action)) {
            if (StringUtils.isEmpty(deviceSn)) {
                return AjaxResult.error("deviceSn 不能为空");
            }
            // [核心修改] 调用 Service 层处理 AI 自动过站逻辑
            bizAssemblyTaskService.completeStepByAi(deviceSn);
            return AjaxResult.success("AI 自动过站信号已处理");
        }
        return AjaxResult.error("未知指令");
    }

    @GetMapping("/check/{deviceSn}")
    public AjaxResult checkDeviceStatus(@PathVariable("deviceSn") String deviceSn)
    {
        return AjaxResult.success(bizAssemblyTaskService.checkDeviceStatus(deviceSn));
    }

    @PreAuthorize("@ss.hasPermi('process:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizAssemblyTask bizAssemblyTask)
    {
        startPage();
        List<BizAssemblyTask> list = bizAssemblyTaskService.selectBizAssemblyTaskList(bizAssemblyTask);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('process:task:export')")
    @Log(title = "装配任务主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizAssemblyTask bizAssemblyTask)
    {
        List<BizAssemblyTask> list = bizAssemblyTaskService.selectBizAssemblyTaskList(bizAssemblyTask);
        ExcelUtil<BizAssemblyTask> util = new ExcelUtil<BizAssemblyTask>(BizAssemblyTask.class);
        util.exportExcel(response, list, "装配任务主数据");
    }

    @PreAuthorize("@ss.hasPermi('process:task:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return AjaxResult.success(bizAssemblyTaskService.selectBizAssemblyTaskById(taskId));
    }

    @PreAuthorize("@ss.hasPermi('process:task:add')")
    @Log(title = "装配任务主", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizAssemblyTask bizAssemblyTask)
    {
        return toAjax(bizAssemblyTaskService.insertBizAssemblyTask(bizAssemblyTask));
    }

    @PreAuthorize("@ss.hasPermi('process:task:edit')")
    @Log(title = "装配任务主", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizAssemblyTask bizAssemblyTask)
    {
        return toAjax(bizAssemblyTaskService.updateBizAssemblyTask(bizAssemblyTask));
    }
}
