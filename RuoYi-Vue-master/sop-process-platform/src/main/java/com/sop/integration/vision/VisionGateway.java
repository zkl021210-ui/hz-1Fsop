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

    /**
     * 重试最近一次视觉服务失败操作
     *
     * @param deviceSn 设备编码
     * @return 重试结果（若无失败事件返回 ok；若缺少上下文返回 fail 并说明原因）
     */
    VisionResult retryLastFailed(String deviceSn);
}
