package com.ruoyi.process.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI 服务自动启动器
 * SpringBoot 启动完成后，自动拉起 Python 脚本。
 * 包含自动重试机制，确保 Python 完全启动后才发送配置。
 */
@Component
public class AiServiceRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AiServiceRunner.class);

    private Process pythonProcess;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.service.python-exe}")
    private String pythonExe;

    @Value("${ai.service.script-path}")
    private String scriptPath;

    @Value("${ai.service.work-dir}")
    private String workDir;

    @Value("${ai.service.api-url}")
    private String pythonApiUrl;

    @Value("${ruoyi.videoPath}")
    private String videoPath;

    @Override
    public void run(String... args) {
        log.info("🤖 [AI服务] 正在准备启动 Python 引擎...");
        log.info("  - Python解释器: {}", pythonExe);
        log.info("  - 脚本路径: {}", scriptPath);
        log.info("  - 工作目录: {}", workDir);

        try {
            if (!new File(pythonExe).exists()) {
                log.error("❌ [AI服务] 启动失败：Python解释器不存在，请检查配置！路径: {}", pythonExe);
                return;
            }
            if (!new File(scriptPath).exists()) {
                log.error("❌ [AI服务] 启动失败：Python脚本不存在，请检查配置！路径: {}", scriptPath);
                return;
            }

            ProcessBuilder pb = new ProcessBuilder(pythonExe, scriptPath);
            pb.directory(new File(workDir));
            pb.redirectErrorStream(true);

            this.pythonProcess = pb.start();

            // 1. 启动日志读取线程（防止 Python 输出缓冲区满了卡死）
            startLoggingThread();

            if (pythonProcess.isAlive()) {
                // ✅ 兼容 Java 8 的代码
                log.info("✅ [AI服务] 进程已创建");

                // 2. 【关键修改】启动一个独立线程，专门负责“死缠烂打”直到通知成功
                // 这样不会阻塞 SpringBoot 的主启动流程
                new Thread(this::notifyPythonVideoPathWithRetry).start();
            } else {
                log.error("❌ [AI服务] 引擎启动失败，进程立即退出。");
            }

        } catch (Exception e) {
            log.error("❌ [AI服务] 启动异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 带重试机制的配置通知
     * 循环尝试连接 Python，直到 Flask 启动完成
     */
    private void notifyPythonVideoPathWithRetry() {
        String url = pythonApiUrl + "/config/update_save_dir";
        log.info("📡 [AI配置] 准备通知 Python 更新视频路径: {}", videoPath);

        Map<String, String> params = new HashMap<>();
        params.put("videoPath", videoPath);

        int maxRetries = 15; // 最多尝试 15 次
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                attempt++;
                // 尝试发送请求
                restTemplate.postForObject(url, params, String.class);

                // 如果代码能走到这里，说明没有抛出异常，连接成功了
                log.info("✅ [第{}次] 成功通知 Python 更新视频路径！", attempt);
                return; // 任务完成，退出线程

            } catch (Exception e) {
                // 捕获连接拒绝异常 (ResourceAccessException)
                if (attempt == 1) {
                    log.info("⏳ Python 尚未就绪，开始轮询等待...");
                }

                // 只有在最后一次尝试失败时才打印 Error，中间只打印 Debug/Info 避免刷屏
                if (attempt >= maxRetries) {
                    log.error("❌ [最终失败] 尝试了 {} 次仍无法连接 Python。请检查 test.py 是否报错退出。", maxRetries);
                } else {
                    try {
                        // 等待 2 秒后重试
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }

    private void startLoggingThread() {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 给 Python 日志加个前缀，方便在控制台区分
                    log.info("🐍 [Python]: {}", line);
                }
            } catch (Exception e) {
                log.warn("... [AI服务] 日志流结束。");
            }
        }).start();
    }

    @PreDestroy
    public void stopPythonService() {
        if (this.pythonProcess != null && this.pythonProcess.isAlive()) {
            log.info("🛑 [AI服务]正在关闭...");
            this.pythonProcess.destroy();
            try {
                if (!this.pythonProcess.waitFor(3, TimeUnit.SECONDS)) {
                    this.pythonProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}