package com.ruoyi.process.domain.dto;

/**
 * 用于向Python服务更新SOP步骤配置的请求体
 */
public class UpdateStepConfigRequest {
    
    private String modelCode;
    private String target;
    private String nextTarget;
    private String deviceSn;
    private String workerName;
    private Long stepId;
    private String stepName; // 新增 stepName 字段

    // Getters and Setters
    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getNextTarget() {
        return nextTarget;
    }

    public void setNextTarget(String nextTarget) {
        this.nextTarget = nextTarget;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    // 新增 stepName 的 getter 和 setter
    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }
}
