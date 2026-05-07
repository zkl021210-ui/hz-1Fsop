package com.sop.mq.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MQ 消息消费日志
 * <p>
 * 记录每条 MQ 消息的消费状态，用于消费幂等校验。
 *
 * @author SOP Team
 */
@Data
@TableName("mq_message_log")
public class MqMessageLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息唯一 ID */
    private String messageId;

    /** 关联流程事件 ID */
    private String eventId;

    /** 事件类型 */
    private String eventType;

    /** 消费者名称 */
    private String consumerName;

    /** 状态：PROCESSING / SUCCESS / FAILED */
    private String status;

    /** 重试次数 */
    private Integer retryCount;

    /** 错误信息 */
    private String errorMessage;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
