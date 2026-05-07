package com.sop.process.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sop.process.domain.AssemblyStepLog;
import com.sop.process.domain.ProcessEvent;
import com.sop.process.domain.SopStep;
import com.sop.process.mapper.AssemblyStepLogMapper;
import com.sop.process.mapper.SopStepMapper;
import com.sop.process.service.ProcessEventService;
import com.sop.process.service.ProcessEventTxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static com.sop.workflow.AssemblyStateMachine.STEP_RUNNING;

/**
 * 步骤超时检测定时任务
 * <p>
 * 扫描长时间处于 RUNNING 状态的步骤日志，超时后记录 STEP_TIMEOUT 事件。
 * 不修改任务/步骤状态，仅做事件记录。
 *
 * @author SOP Team
 */
@Slf4j
@Component
public class StepTimeoutCheckScheduler {

    private final AssemblyStepLogMapper assemblyStepLogMapper;
    private final SopStepMapper sopStepMapper;
    private final ProcessEventService processEventService;
    private final ProcessEventTxService processEventTxService;

    @Value("${sop.timeout.step-multiplier:2.0}")
    private double stepMultiplier;

    public StepTimeoutCheckScheduler(AssemblyStepLogMapper assemblyStepLogMapper,
                                     SopStepMapper sopStepMapper,
                                     ProcessEventService processEventService,
                                     ProcessEventTxService processEventTxService) {
        this.assemblyStepLogMapper = assemblyStepLogMapper;
        this.sopStepMapper = sopStepMapper;
        this.processEventService = processEventService;
        this.processEventTxService = processEventTxService;
    }

    @Scheduled(fixedDelayString = "${sop.timeout.check-interval:60000}")
    public void checkStepTimeout() {
        log.debug("开始步骤超时检测扫描");

        List<AssemblyStepLog> runningLogs = assemblyStepLogMapper.selectList(
                new LambdaQueryWrapper<AssemblyStepLog>()
                        .eq(AssemblyStepLog::getStatus, STEP_RUNNING)
                        .eq(AssemblyStepLog::getDeleted, 0)
                        .isNotNull(AssemblyStepLog::getStartTime));

        if (runningLogs.isEmpty()) {
            log.debug("无 RUNNING 状态的步骤日志");
            return;
        }

        int timeoutCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (AssemblyStepLog stepLog : runningLogs) {
            if (stepLog.getStartTime() == null) continue;

            SopStep step = sopStepMapper.selectById(stepLog.getStepId());
            if (step == null || step.getStandardDuration() == null || step.getStandardDuration() <= 0) continue;

            long maxDurationSec = (long) (step.getStandardDuration() * stepMultiplier);
            long elapsedSec = Duration.between(stepLog.getStartTime(), now).getSeconds();

            if (elapsedSec <= maxDurationSec) continue;

            String timeoutEventId = "STEP_TIMEOUT:" + stepLog.getId();

            ProcessEvent existing = processEventService.getByEventId(timeoutEventId);
            if (existing != null) continue;

            String payload = String.format(
                    "{\"stepLogId\":%d,\"taskId\":%d,\"stepId\":%d,\"stepNo\":%d,\"elapsedSec\":%d,\"maxDurationSec\":%d,\"startTime\":\"%s\"}",
                    stepLog.getId(), stepLog.getTaskId(), stepLog.getStepId(),
                    stepLog.getStepNo(), elapsedSec, maxDurationSec, stepLog.getStartTime());

            processEventTxService.recordStepTimeout(timeoutEventId, stepLog.getTaskId(),
                    stepLog.getSerialNumber(), stepLog.getStepId(), stepLog.getId(), payload);
            timeoutCount++;

            log.warn("步骤超时: taskId={}, stepLogId={}, stepNo={}, elapsed={}s, max={}s",
                    stepLog.getTaskId(), stepLog.getId(), stepLog.getStepNo(), elapsedSec, maxDurationSec);
        }

        if (timeoutCount > 0) {
            log.info("步骤超时检测完成: 发现 {} 个超时步骤", timeoutCount);
        }
    }
}
