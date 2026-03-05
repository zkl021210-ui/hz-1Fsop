package com.ruoyi.process.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 用于接收Python服务停止录制命令的响应体
 */
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略未知的JSON字段
public class StopRecordResponse {

    private String msg;
    private String filename;

    // Getters and Setters
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
