package com.sop.integration.vision;

import com.sop.common.exception.BusinessException;
import com.sop.common.exception.ErrorCode;
import com.sop.process.service.ProcessEventTxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    public PythonVisionGateway(VisionProperties visionProperties,
                               ProcessEventTxService processEventTxService) {
        this.visionProperties = visionProperties;
        this.restTemplate = new RestTemplate();
        this.processEventTxService = processEventTxService;
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
            processEventTxService.recordError(body instanceof StartRecordCommand ? ((StartRecordCommand) body).getDeviceSn()
                    : body instanceof StopRecordCommand ? ((StopRecordCommand) body).getDeviceSn()
                    : body instanceof VisionStepConfigCommand ? ((VisionStepConfigCommand) body).getDeviceSn()
                    : "unknown", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("视觉服务调用异常: path={}", path, e);
            processEventTxService.recordError(body instanceof StartRecordCommand ? ((StartRecordCommand) body).getDeviceSn()
                    : body instanceof StopRecordCommand ? ((StopRecordCommand) body).getDeviceSn()
                    : body instanceof VisionStepConfigCommand ? ((VisionStepConfigCommand) body).getDeviceSn()
                    : "unknown", e.getMessage());
            throw new BusinessException(ErrorCode.VISION_SERVICE_ERROR, "视觉服务调用异常: " + e.getMessage());
        }
    }
}
