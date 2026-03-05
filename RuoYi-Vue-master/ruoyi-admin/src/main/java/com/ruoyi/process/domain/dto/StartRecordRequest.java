package com.ruoyi.process.domain.dto;

/**
 * 用于向Python服务发送开始录制命令的请求体
 */
public class StartRecordRequest {

    private String filename;

    public StartRecordRequest(String filename) {
        this.filename = filename;
    }

    // Getters and Setters
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
