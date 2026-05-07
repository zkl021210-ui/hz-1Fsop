package com.sop.process.controller;

import com.sop.common.result.ApiResult;
import com.sop.integration.vision.VisionGateway;
import com.sop.integration.vision.VisionResult;
import com.sop.process.domain.ProcessEvent;
import com.sop.process.service.ProcessEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视觉服务异常补偿接口
 *
 * @author SOP Team
 */
@Slf4j
@RestController
@RequestMapping("/api/process/vision")
@Tag(name = "视觉服务异常补偿", description = "查询和重试视觉服务失败操作")
public class VisionRetryController {

    private final ProcessEventService processEventService;
    private final VisionGateway visionGateway;

    public VisionRetryController(ProcessEventService processEventService,
                                 VisionGateway visionGateway) {
        this.processEventService = processEventService;
        this.visionGateway = visionGateway;
    }

    /**
     * 查询指定设备的视觉服务失败事件
     */
    @GetMapping("/failed-events/{deviceSn}")
    @Operation(summary = "查询设备失败事件", description = "查询指定设备最近10条视觉服务失败事件")
    public ApiResult<List<ProcessEvent>> listFailedEvents(@PathVariable String deviceSn) {
        List<ProcessEvent> events = processEventService.listFailedVisionErrors(deviceSn);
        return ApiResult.success(events);
    }

    /**
     * 查询所有视觉服务失败事件
     */
    @GetMapping("/failed-events")
    @Operation(summary = "查询所有失败事件", description = "查询最近20条视觉服务失败事件")
    public ApiResult<List<ProcessEvent>> listAllFailedEvents() {
        List<ProcessEvent> events = processEventService.listAllFailedVisionErrors();
        return ApiResult.success(events);
    }

    /**
     * 手动重试指定设备的最近一次视觉服务失败操作
     */
    @PostMapping("/retry/{deviceSn}")
    @Operation(summary = "重试视觉服务", description = "重试指定设备最近一次失败的视觉服务操作。若缺少上下文则返回不可重试原因")
    public ApiResult<VisionResult> retryLastFailed(@PathVariable String deviceSn) {
        log.info("手动重试视觉服务: deviceSn={}", deviceSn);
        VisionResult result = visionGateway.retryLastFailed(deviceSn);
        return ApiResult.success(result);
    }
}
