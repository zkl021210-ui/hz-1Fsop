package com.sop.integration.vision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sop.common.exception.BusinessException;
import com.sop.common.exception.ErrorCode;
import com.sop.process.domain.ProcessEvent;
import com.sop.process.service.ProcessEventService;
import com.sop.process.service.ProcessEventTxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Python 视觉服务 HTTP 调用实现
 *
 * @author SOP Team
 */
@Slf4j
@Component
public class PythonVisionGateway implements VisionGateway {

    private final VisionProperties visionProperties;
    private final RestTemplate restTemplate;
    private final ProcessEventTxService processEventTxService;
    private final ProcessEventService processEventService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PythonVisionGateway(VisionProperties visionProperties,
                               ProcessEventTxService processEventTxService,
                               ProcessEventService processEventService) {
        this.visionProperties = visionProperties;
        this.restTemplate = new RestTemplate();
        this.processEventTxService = processEventTxService;
        this.processEventService = processEventService;
    }

    @Override
    public VisionResult updateStepConfig(VisionStepConfigCommand command) {
        if (!visionProperties.isEnabled()) {
            log.info("视觉服务未启用，跳过 updateStepConfig: deviceSn={}", command.getDeviceSn());
            return VisionResult.ok();
        }
        return post("/api/vision/step-config", command);
    }

    @Override
    public VisionResult startRecord(StartRecordCommand command) {
        if (!visionProperties.isEnabled()) {
            log.info("视觉服务未启用，跳过 startRecord: deviceSn={}", command.getDeviceSn());
            return VisionResult.ok();
        }
        return post("/api/vision/start-record", command);
    }

    @Override
    public VisionResult stopRecord(StopRecordCommand command) {
        if (!visionProperties.isEnabled()) {
            log.info("视觉服务未启用，跳过 stopRecord: deviceSn={}", command.getDeviceSn());
            return VisionResult.ok();
        }
        return post("/api/vision/stop-record", command);
    }

    @Override
    public VisionHealthStatus healthCheck() {
        try {
            String url = visionProperties.getBaseUrl() + "/api/vision/health";
            return restTemplate.getForObject(url, VisionHealthStatus.class);
        } catch (Exception e) {
            log.error("视觉服务健康检查失败", e);
            VisionHealthStatus status = new VisionHealthStatus();
            status.setAvailable(false);
            return status;
        }
    }

    private VisionResult post(String path, Object body) {
        try {
            String url = visionProperties.getBaseUrl() + path;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            VisionResult result = restTemplate.postForObject(url, entity, VisionResult.class);
            if (result == null || !result.isSuccess()) {
                String errMsg = result != null ? result.getErrorMessage() : "视觉服务返回空结果";
                log.error("视觉服务调用失败: path={}, error={}", path, errMsg);
                throw new BusinessException(ErrorCode.VISION_SERVICE_ERROR, errMsg);
            }
            return result;
        } catch (BusinessException e) {
            recordVisionCallError(path, body, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("视觉服务调用异常: path={}", path, e);
            recordVisionCallError(path, body, e.getMessage());
            throw new BusinessException(ErrorCode.VISION_SERVICE_ERROR, "视觉服务调用异常: " + e.getMessage());
        }
    }

    private void recordVisionCallError(String path, Object body, String errorMsg) {
        String deviceSn = body instanceof StartRecordCommand ? ((StartRecordCommand) body).getDeviceSn()
                : body instanceof StopRecordCommand ? ((StopRecordCommand) body).getDeviceSn()
                : body instanceof VisionStepConfigCommand ? ((VisionStepConfigCommand) body).getDeviceSn()
                : "unknown";
        String errorType = path.replace("/api/vision/", "");
        String payload = toJson(body);
        processEventTxService.recordVisionError(deviceSn, errorMsg, errorType, payload);
    }

    @Override
    public VisionResult retryLastFailed(String deviceSn) {
        List<ProcessEvent> failedEvents = processEventService.listFailedVisionErrors(deviceSn);
        if (failedEvents.isEmpty()) {
            log.info("无失败视觉事件可重试: deviceSn={}", deviceSn);
            return VisionResult.ok();
        }
        ProcessEvent latest = failedEvents.get(0);

        String errorType = latest.getResult();
        if (errorType == null || errorType.isEmpty()) {
            String reason = "不可自动重试：缺少操作类型（errorType），无法判断失败的是哪个接口";
            log.warn("{}: deviceSn={}, eventId={}", reason, deviceSn, latest.getEventId());
            return VisionResult.fail("RETRY_NOT_POSSIBLE", reason);
        }

        String payload = latest.getPayload();
        if (payload == null || payload.isEmpty()) {
            String reason = "不可自动重试：缺少原始请求载荷（payload），无法重建请求参数";
            log.warn("{}: deviceSn={}, eventId={}", reason, deviceSn, latest.getEventId());
            return VisionResult.fail("RETRY_NOT_POSSIBLE", reason);
        }

        try {
            Object command = parsePayload(errorType, payload);
            if (command == null) {
                return VisionResult.fail("RETRY_NOT_POSSIBLE",
                        "不可自动重试：无法解析操作类型 " + errorType + " 的请求载荷");
            }
            String path = "/api/vision/" + errorType;
            VisionResult result = post(path, command);
            if (result.isSuccess()) {
                processEventTxService.markSuccess(latest.getEventId(), "手动重试成功");
                log.info("视觉服务重试成功: deviceSn={}, eventId={}, errorType={}", deviceSn, latest.getEventId(), errorType);
            }
            return result;
        } catch (BusinessException e) {
            log.error("视觉服务重试仍失败: deviceSn={}, eventId={}", deviceSn, latest.getEventId(), e);
            throw e;
        } catch (Exception e) {
            log.error("视觉服务重试异常: deviceSn={}, eventId={}", deviceSn, latest.getEventId(), e);
            throw new BusinessException(ErrorCode.VISION_SERVICE_ERROR, "重试视觉服务异常: " + e.getMessage());
        }
    }

    private Object parsePayload(String errorType, String payload) {
        try {
            switch (errorType) {
                case "step-config":
                    return objectMapper.readValue(payload, VisionStepConfigCommand.class);
                case "start-record":
                    return objectMapper.readValue(payload, StartRecordCommand.class);
                case "stop-record":
                    return objectMapper.readValue(payload, StopRecordCommand.class);
                default:
                    log.warn("未知视觉操作类型: {}", errorType);
                    return null;
            }
        } catch (Exception e) {
            log.error("解析视觉请求载荷失败: errorType={}", errorType, e);
            return null;
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
