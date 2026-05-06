package com.sop.process.controller;

import com.sop.common.page.PageResult;
import com.sop.common.result.ApiResult;
import com.sop.process.command.*;
import com.sop.process.dto.AssemblyTaskDTO;
import com.sop.process.service.AssemblyTaskService;
import com.sop.process.service.SopExecutionService;
import com.sop.process.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "装配任务管理")
@RestController
@RequestMapping("/api/process/assembly-task")
public class AssemblyTaskController {

    private final AssemblyTaskService assemblyTaskService;
    private final SopExecutionService sopExecutionService;

    public AssemblyTaskController(AssemblyTaskService assemblyTaskService,
                                  SopExecutionService sopExecutionService) {
        this.assemblyTaskService = assemblyTaskService;
        this.sopExecutionService = sopExecutionService;
    }

    // ==================== 基础 CRUD ====================

    @Operation(summary = "分页查询装配任务")
    @GetMapping("/page")
    public ApiResult<PageResult<AssemblyTaskVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String status) {
        return ApiResult.success(PageResult.from(page, pageSize,
                assemblyTaskService.pageAssemblyTasks(page, pageSize, modelCode, status)));
    }

    @Operation(summary = "查询装配任务详情")
    @GetMapping("/{taskId}")
    public ApiResult<AssemblyTaskVO> getById(@PathVariable Long taskId) {
        return ApiResult.success(assemblyTaskService.getAssemblyTaskById(taskId));
    }

    @Operation(summary = "新增装配任务")
    @PostMapping
    public ApiResult<AssemblyTaskVO> create(@RequestBody AssemblyTaskDTO dto) {
        return ApiResult.success(assemblyTaskService.createAssemblyTask(dto));
    }

    @Operation(summary = "修改装配任务")
    @PutMapping("/{taskId}")
    public ApiResult<AssemblyTaskVO> update(@PathVariable Long taskId, @RequestBody AssemblyTaskDTO dto) {
        return ApiResult.success(assemblyTaskService.updateAssemblyTask(taskId, dto));
    }

    @Operation(summary = "删除装配任务")
    @DeleteMapping("/{taskId}")
    public ApiResult<Void> delete(@PathVariable Long taskId) {
        assemblyTaskService.deleteAssemblyTask(taskId);
        return ApiResult.success();
    }

    // ==================== 核心流程接口 ====================

    @Operation(summary = "开工")
    @PostMapping("/start")
    public ApiResult<StartTaskResultVO> startTask(@Valid @RequestBody StartTaskCommand command) {
        return ApiResult.success(sopExecutionService.startTask(command));
    }

    @Operation(summary = "启动步骤")
    @PostMapping("/{taskId}/steps/{stepId}/start")
    public ApiResult<StartStepResultVO> startStep(@PathVariable Long taskId, @PathVariable Long stepId) {
        return ApiResult.success(sopExecutionService.startStep(taskId, stepId));
    }

    @Operation(summary = "AI自动过站回调")
    @PostMapping("/ai-next-step")
    public ApiResult<AiNextStepResultVO> aiNextStep(@RequestBody AiNextStepCallbackCommand command) {
        return ApiResult.success(sopExecutionService.handleAiNextStep(command));
    }

    @Operation(summary = "手动完成任务")
    @PostMapping("/{taskId}/finish")
    public ApiResult<FinishTaskResultVO> finishTask(@PathVariable Long taskId,
                                                     @RequestBody(required = false) FinishTaskCommand command) {
        String reason = command != null ? command.getReason() : null;
        return ApiResult.success(sopExecutionService.finishTask(taskId, reason));
    }
}
