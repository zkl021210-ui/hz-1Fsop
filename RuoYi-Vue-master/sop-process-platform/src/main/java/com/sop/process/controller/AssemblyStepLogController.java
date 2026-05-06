package com.sop.process.controller;

import com.sop.common.page.PageResult;
import com.sop.common.result.ApiResult;
import com.sop.process.dto.AssemblyStepLogDTO;
import com.sop.process.service.AssemblyStepLogService;
import com.sop.process.vo.AssemblyStepLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "装配步骤日志管理")
@RestController
@RequestMapping("/api/process/assembly-step-log")
public class AssemblyStepLogController {

    private final AssemblyStepLogService assemblyStepLogService;

    public AssemblyStepLogController(AssemblyStepLogService assemblyStepLogService) {
        this.assemblyStepLogService = assemblyStepLogService;
    }

    @Operation(summary = "分页查询装配步骤日志")
    @GetMapping("/page")
    public ApiResult<PageResult<AssemblyStepLogVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String status) {
        return ApiResult.success(PageResult.from(page, pageSize,
                assemblyStepLogService.pageAssemblyStepLogs(page, pageSize, taskId, status)));
    }

    @Operation(summary = "查询装配步骤日志详情")
    @GetMapping("/{id}")
    public ApiResult<AssemblyStepLogVO> getById(@PathVariable Long id) {
        return ApiResult.success(assemblyStepLogService.getAssemblyStepLogById(id));
    }

    @Operation(summary = "新增装配步骤日志")
    @PostMapping
    public ApiResult<AssemblyStepLogVO> create(@RequestBody AssemblyStepLogDTO dto) {
        return ApiResult.success(assemblyStepLogService.createAssemblyStepLog(dto));
    }

    @Operation(summary = "修改装配步骤日志")
    @PutMapping("/{id}")
    public ApiResult<AssemblyStepLogVO> update(@PathVariable Long id, @RequestBody AssemblyStepLogDTO dto) {
        return ApiResult.success(assemblyStepLogService.updateAssemblyStepLog(id, dto));
    }

    @Operation(summary = "删除装配步骤日志")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        assemblyStepLogService.deleteAssemblyStepLog(id);
        return ApiResult.success();
    }
}
