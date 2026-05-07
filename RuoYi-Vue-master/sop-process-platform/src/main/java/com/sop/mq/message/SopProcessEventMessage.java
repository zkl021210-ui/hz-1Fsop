package com.sop.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SOP 过程事件消息
 * <p>
 * 通过 RabbitMQ 发送的事件消息体，messageId 用于消费幂等。
 *
 * @author SOP Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SopProcessEventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息唯一 ID（生成规则：MSG:{eventId} 或 MSG:{eventType}:{sn}:{taskId}:{stepRunId}:{timestamp}） */
    private String messageId;

    /** 对应 process_event.event_id */
    private String eventId;

    /** 事件类型 */
    private String eventType;

    /** 路由键 */
    private String routingKey;

    /** 关联任务 ID */
    private Long taskId;

    /** 设备编码 */
    private String deviceSn;

    /** 步骤 ID */
    private Long stepId;

    /** 步骤日志 ID */
    private Long stepRunId;

    /** 载荷（JSON） */
    private String payload;

    /** 事件发生时间 */
    private LocalDateTime occurredTime;

    /** 重试次数 */
    private Integer retryCount;
}
