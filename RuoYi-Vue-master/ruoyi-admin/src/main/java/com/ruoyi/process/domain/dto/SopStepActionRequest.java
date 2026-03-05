package com.ruoyi.process.domain.dto;
import java.util.List;
public class SopStepActionRequest {
    private Long stepId;
    private String modelCode;
    private String deviceSn;
    private Long id; // 停止时用的 LogID
    private Integer assemblyRound;
    private String workerName;
    private String stepName;
    private Integer stepOrder; // 新增：步骤顺序号


    // AI配置参数
    private String target;
    private List<String> historyTargets;
    private String nextTarget;
    private String container;
    private String status;

    // Getters and Setters ...
    public Long getStepId() { return stepId; }
    public void setStepId(Long stepId) { this.stepId = stepId; }
    public String getModelCode() { return modelCode; }
    public void setModelCode(String modelCode) { this.modelCode = modelCode; }
    public String getDeviceSn() { return deviceSn; }
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getNextTarget() { return nextTarget; }
    public void setNextTarget(String nextTarget) { this.nextTarget = nextTarget; }
    public String getContainer() { return container; }
    public void setContainer(String container) { this.container = container; }
    public Integer getAssemblyRound() { return assemblyRound; }
    public void setAssemblyRound(Integer assemblyRound) { this.assemblyRound = assemblyRound; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }
    public List<String> getHistoryTargets() {return historyTargets;}
    public void setHistoryTargets(List<String> historyTargets) {this.historyTargets = historyTargets;}


}