package com.sop.process.dto;

import lombok.Data;

/**
 * 工人 DTO
 * <p>
 * 用于新增/修改工人信息时的请求参数传输。
 *
 * @author SOP Team
 */
@Data
public class WorkerDTO {

    /** 姓名 */
    private String workerName;

    /** 工号 */
    private String workerCode;

    /** 班组 */
    private String teamName;

    /** 状态（0-正常 1-停用） */
    private String status;
}
