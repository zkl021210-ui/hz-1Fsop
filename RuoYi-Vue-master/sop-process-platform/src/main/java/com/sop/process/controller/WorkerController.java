package com.sop.process.controller;

import com.sop.common.page.PageResult;
import com.sop.common.result.ApiResult;
import com.sop.process.dto.WorkerDTO;
import com.sop.process.service.WorkerService;
import com.sop.process.vo.WorkerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "工人管理")
@RestController
@RequestMapping("/api/process/worker")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @Operation(summary = "分页查询工人")
    @GetMapping("/page")
    public ApiResult<PageResult<WorkerVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String workerName,
            @RequestParam(required = false) String status) {
        return ApiResult.success(PageResult.from(page, pageSize,
                workerService.pageWorkers(page, pageSize, workerName, status)));
    }

    @Operation(summary = "查询工人详情")
    @GetMapping("/{workerId}")
    public ApiResult<WorkerVO> getById(@PathVariable Long workerId) {
        return ApiResult.success(workerService.getWorkerById(workerId));
    }

    @Operation(summary = "新增工人")
    @PostMapping
    public ApiResult<WorkerVO> create(@RequestBody WorkerDTO dto) {
        return ApiResult.success(workerService.createWorker(dto));
    }

    @Operation(summary = "修改工人")
    @PutMapping("/{workerId}")
    public ApiResult<WorkerVO> update(@PathVariable Long workerId, @RequestBody WorkerDTO dto) {
        return ApiResult.success(workerService.updateWorker(workerId, dto));
    }

    @Operation(summary = "删除工人")
    @DeleteMapping("/{workerId}")
    public ApiResult<Void> delete(@PathVariable Long workerId) {
        workerService.deleteWorker(workerId);
        return ApiResult.success();
    }
}
