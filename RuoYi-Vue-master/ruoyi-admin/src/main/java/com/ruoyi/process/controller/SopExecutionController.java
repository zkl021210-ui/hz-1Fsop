package com.ruoyi.process.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.process.domain.dto.SopStepActionRequest;
import com.ruoyi.process.service.IBizAssemblyTaskService; // ✅ 引入 TaskService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SOP 执行控制器 (修正版)
 * 核心职责：只做路由转发，业务逻辑全部下沉到 Service
 */
@RestController
@RequestMapping("/process/sop")
public class SopExecutionController extends BaseController {

    // ❌ 删除：不要在这里直接操作日志和Python
    // @Autowired private IBizAssemblyLogService logService;
    // @Autowired private PythonInteractionService pythonService;

    // ✅ 新增：只依赖 TaskService，它包含了完整的防重和流转逻辑
    @Autowired
    private IBizAssemblyTaskService taskService;

    /**
     * 开始步骤
     * 逻辑下沉：查重 -> 补全工人姓名 -> 开启录像 -> 插入/复用日志
     */
    @PostMapping("/step/start")
    public AjaxResult startStep(@RequestBody SopStepActionRequest req) {
        Long logId = taskService.startSopStep(req);
        return AjaxResult.success(logId);
    }

    /**
     * 停止/暂停步骤
     * 逻辑下沉：停止录像 -> 获取文件名 -> 更新状态(完成/暂停) -> 保存
     */
    @PostMapping("/step/stop")
    public AjaxResult stopStep(@RequestBody SopStepActionRequest req) {
        taskService.stopSopStep(req);
        return AjaxResult.success();
    }
}