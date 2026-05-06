package com.sop.integration.vision;

/**
 * 视觉服务调用网关
 * <p>
 * 所有外部 Python 视觉服务调用必须通过此接口，不允许在业务 Service 中直接拼 URL 或使用 HTTP 客户端。
 *
 * @author SOP Team
 */
public interface VisionGateway {

    /**
     * 下发步骤配置到视觉服务
     */
    VisionResult updateStepConfig(VisionStepConfigCommand command);

    /**
     * 启动录像
     */
    VisionResult startRecord(StartRecordCommand command);

    /**
     * 停止录像
     */
    VisionResult stopRecord(StopRecordCommand command);

    /**
     * 健康检查
     */
    VisionHealthStatus healthCheck();
}
