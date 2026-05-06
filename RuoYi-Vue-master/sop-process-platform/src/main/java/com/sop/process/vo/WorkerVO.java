package com.sop.process.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工人 VO
 * <p>
 * 用于返回给前端的工人视图数据。
 *
 * @author SOP Team
 */
@Data
public class WorkerVO {

    private Long workerId;
    private String workerName;
    private String workerCode;
    private String teamName;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
