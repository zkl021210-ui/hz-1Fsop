package com.sop.process.controller;

import com.sop.common.page.PageResult;
import com.sop.common.result.ApiResult;
import com.sop.process.dto.SopStepDTO;
import com.sop.process.service.SopStepService;
import com.sop.process.vo.SopStepVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SOP步骤管理")
@RestController
@RequestMapping("/api/process/sop-step")
public class SopStepController {

    private final SopStepService sopStepService;

    public SopStepController(SopStepService sopStepService) {
        this.sopStepService = sopStepService;
    }

    @Operation(summary = "分页查询SOP步骤")
    @GetMapping("/page")
    public ApiResult<PageResult<SopStepVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String processStage) {
        return ApiResult.success(PageResult.from(page, pageSize,
                sopStepService.pageSopSteps(page, pageSize, modelCode, processStage)));
    }

    @Operation(summary = "根据产品型号编码查询SOP步骤列表")
    @GetMapping("/list-by-model")
    public ApiResult<List<SopStepVO>> listByModelCode(@RequestParam String modelCode) {
        return ApiResult.success(sopStepService.listByModelCode(modelCode));
    }

    @Operation(summary = "查询SOP步骤详情")
    @GetMapping("/{stepId}")
    public ApiResult<SopStepVO> getById(@PathVariable Long stepId) {
        return ApiResult.success(sopStepService.getSopStepById(stepId));
    }

    @Operation(summary = "新增SOP步骤")
    @PostMapping
    public ApiResult<SopStepVO> create(@RequestBody SopStepDTO dto) {
        return ApiResult.success(sopStepService.createSopStep(dto));
    }

    @Operation(summary = "修改SOP步骤")
    @PutMapping("/{stepId}")
    public ApiResult<SopStepVO> update(@PathVariable Long stepId, @RequestBody SopStepDTO dto) {
        return ApiResult.success(sopStepService.updateSopStep(stepId, dto));
    }

    @Operation(summary = "删除SOP步骤")
    @DeleteMapping("/{stepId}")
    public ApiResult<Void> delete(@PathVariable Long stepId) {
        sopStepService.deleteSopStep(stepId);
        return ApiResult.success();
    }
}
