package com.ruoyi.process.service;

import com.ruoyi.process.domain.dto.SopStepActionRequest;
import com.ruoyi.process.domain.dto.StopRecordResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class PythonInteractionService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.service.api-url}")
    private String pythonApiUrl;

    public void startRecording(String filename) {
        String recordUrl = pythonApiUrl + "/record/start";
        Map<String, String> recordParams = new HashMap<>();
        recordParams.put("filename", filename);
        try {
            restTemplate.postForObject(recordUrl, recordParams, String.class);
            System.out.println("[Python] 已发送录像指令: " + filename);
        } catch (Exception e) {
            System.err.println(" 录像启动失败: " + e.getMessage());
        }
    }

    /**
     * 【原有】完整版：更新 AI 配置 + 启动录像
     */
    public void startStepRecording(SopStepActionRequest req, String filename) {
        // 1. 发送 AI 配置
        String configUrl = pythonApiUrl + "/update_step_config";
        Map<String, Object> configParams = new HashMap<>();
        configParams.put("modelCode", req.getModelCode());
        configParams.put("target", req.getTarget());
        configParams.put("nextTarget", req.getNextTarget());
        configParams.put("deviceSn", req.getDeviceSn());
        configParams.put("stepId", req.getStepId());

        try {
            restTemplate.postForObject(configUrl, configParams, String.class);
            System.out.println(" [Python] AI配置更新成功: " + req.getDeviceSn());
        } catch (Exception e) {
            System.err.println(" AI配置更新失败: " + e.getMessage());
        }

        // 2. 调用上面的简化版开始录像
        this.startRecording(filename);
    }

    public String stopRecording() {
        String url = pythonApiUrl + "/record/stop";
        try {
            // 注意：StopRecordResponse 中的字段名必须是 filename (全小写)
            StopRecordResponse response = restTemplate.postForObject(url, null, StopRecordResponse.class);
            if (response != null && response.getFilename() != null) {
                System.out.println(" [Python] 录制完成，文件名: " + response.getFilename());
                return response.getFilename();
            }
            return null;
        } catch (Exception e) {
            System.err.println(" 停止录像请求异常: " + e.getMessage());
            return null;
        }
    }
}