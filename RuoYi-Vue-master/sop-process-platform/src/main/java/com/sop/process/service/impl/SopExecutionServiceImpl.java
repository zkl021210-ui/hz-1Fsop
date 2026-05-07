package com.sop.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sop.common.exception.BusinessException;
import com.sop.common.exception.ErrorCode;
import com.sop.infrastructure.lock.DeviceLock;
import com.sop.integration.vision.*;
import com.sop.process.command.*;
import com.sop.process.domain.*;
import com.sop.process.mapper.*;
import com.sop.process.service.*;
import com.sop.process.vo.*;
import com.sop.workflow.AssemblyStateMachine;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.sop.workflow.AssemblyStateMachine.*;

/**
 * SOP 装配执行服务实现
 * <p>
 * 核心业务流程编排。
 *
 * @author SOP Team
 */
@Slf4j
@Service
public class SopExecutionServiceImpl implements SopExecutionService {

    private final AssemblyTaskMapper assemblyTaskMapper;
    private final AssemblyStepLogMapper assemblyStepLogMapper;
    private final SopStepMapper sopStepMapper;
    private final WorkerMapper workerMapper;
    private final QualityTraceMapper qualityTraceMapper;
    private final AssemblyStateMachine stateMachine;
    private final VisionGateway visionGateway;
    private final ProcessEventService processEventService;
    private final ProcessEventTxService processEventTxService;
    private final DeviceLock deviceLock;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SopExecutionServiceImpl(AssemblyTaskMapper assemblyTaskMapper,
                                   AssemblyStepLogMapper assemblyStepLogMapper,
                                   SopStepMapper sopStepMapper,
                                   WorkerMapper workerMapper,
                                   QualityTraceMapper qualityTraceMapper,
                                   AssemblyStateMachine stateMachine,
                                   VisionGateway visionGateway,
                                   ProcessEventService processEventService,
                                   ProcessEventTxService processEventTxService,
                                   DeviceLock deviceLock) {
        this.assemblyTaskMapper = assemblyTaskMapper;
        this.assemblyStepLogMapper = assemblyStepLogMapper;
        this.sopStepMapper = sopStepMapper;
        this.workerMapper = workerMapper;
        this.qualityTraceMapper = qualityTraceMapper;
        this.stateMachine = stateMachine;
        this.visionGateway = visionGateway;
        this.processEventService = processEventService;
        this.processEventTxService = processEventTxService;
        this.deviceLock = deviceLock;
    }

    // ==================== 开工 ====================

    @Override
    @Transactional
    public StartTaskResultVO startTask(StartTaskCommand command) {
        String deviceSn = command.getDeviceSn();
        if (!deviceLock.tryLock(deviceSn)) {
            throw new BusinessException(ErrorCode.SERVICE_UNAVAILABLE,
                    "设备 " + deviceSn + " 正在处理中，请稍后重试");
        }
        try {
            return doStartTask(command, deviceSn);
        } finally {
            deviceLock.unlock(deviceSn);
        }
    }

    private StartTaskResultVO doStartTask(StartTaskCommand command, String deviceSn) {
        String modelCode = command.getModelCode();
        Long workerId = command.getWorkerId();

        // 1. 查询 SOP 步骤
        List<SopStep> steps = sopStepMapper.selectList(
                new LambdaQueryWrapper<SopStep>()
                        .eq(SopStep::getModelCode, modelCode)
                        .eq(SopStep::getDeleted, 0)
                        .orderByAsc(SopStep::getStepOrder));
        if (steps.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_SOP_STEPS, "型号 " + modelCode + " 未配置SOP步骤");
        }

        // 2. 校验同设备无进行中任务
        Long runningCount = assemblyTaskMapper.selectCount(
                new LambdaQueryWrapper<AssemblyTask>()
                        .eq(AssemblyTask::getDeviceSn, deviceSn)
                        .eq(AssemblyTask::getDeleted, 0)
                        .eq(AssemblyTask::getStatus, TASK_RUNNING));
        if (runningCount > 0) {
            throw new BusinessException(ErrorCode.TASK_RUNNING_EXISTS,
                    "设备 " + deviceSn + " 已有进行中的装配任务");
        }

        // 3. 查询工人姓名
        Worker worker = workerMapper.selectById(workerId);
        String workerName = worker != null ? worker.getWorkerName() : "";

        // 4. 创建任务
        AssemblyTask task = new AssemblyTask();
        task.setDeviceSn(deviceSn);
        task.setModelCode(modelCode);
        task.setWorkerId(workerId);
        task.setWorkerName(workerName);
        task.setCurrentStepIndex(steps.get(0).getStepOrder());
        task.setStatus(TASK_RUNNING);
        task.setAssemblyRound(1);
        task.setStartTime(LocalDateTime.now());
        stateMachine.checkTaskTransition(TASK_WAITING_START, EVENT_START_TASK);
        assemblyTaskMapper.insert(task);

        log.info("开工成功: taskId={}, deviceSn={}, modelCode={}", task.getTaskId(), deviceSn, modelCode);

        processEventTxService.recordTaskStarted(task.getTaskId(), deviceSn, modelCode);

        StartTaskResultVO vo = new StartTaskResultVO();
        vo.setTaskId(task.getTaskId());
        vo.setDeviceSn(deviceSn);
        vo.setModelCode(modelCode);
        vo.setCurrentStepNo(task.getCurrentStepIndex());
        vo.setStatus(task.getStatus());
        vo.setStartTime(task.getStartTime());
        return vo;
    }

    // ==================== 启动步骤 ====================

    @Override
    @Transactional
    public StartStepResultVO startStep(Long taskId, Long stepId) {
        // 1. 查询任务
        AssemblyTask task = assemblyTaskMapper.selectById(taskId);
        if (task == null || task.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "装配任务不存在");
        }
        if (!TASK_RUNNING.equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.TASK_NOT_RUNNING, "任务状态不是进行中，无法启动步骤");
        }

        String deviceSn = task.getDeviceSn();
        if (!deviceLock.tryLock(deviceSn)) {
            throw new BusinessException(ErrorCode.SERVICE_UNAVAILABLE,
                    "设备 " + deviceSn + " 正在处理中，请稍后重试");
        }
        try {
            return doStartStep(task, stepId);
        } finally {
            deviceLock.unlock(deviceSn);
        }
    }

    private StartStepResultVO doStartStep(AssemblyTask task, Long stepId) {
        // 2. 查询步骤
        SopStep step = sopStepMapper.selectById(stepId);
        if (step == null || step.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "SOP步骤不存在");
        }
        if (!step.getModelCode().equals(task.getModelCode())) {
            throw new BusinessException(ErrorCode.STEP_NOT_MATCH, "步骤所属型号与任务型号不一致");
        }
        if (!step.getStepOrder().equals(task.getCurrentStepIndex())) {
            throw new BusinessException(ErrorCode.STEP_NOT_MATCH,
                    "当前步骤序号 " + step.getStepOrder() + " 与任务当前步骤 " + task.getCurrentStepIndex() + " 不匹配");
        }

        // 3. 查找或创建步骤日志
        AssemblyStepLog stepLog = assemblyStepLogMapper.selectOne(
                new LambdaQueryWrapper<AssemblyStepLog>()
                        .eq(AssemblyStepLog::getTaskId, task.getTaskId())
                        .eq(AssemblyStepLog::getStepId, stepId)
                        .eq(AssemblyStepLog::getDeleted, 0));

        if (stepLog == null) {
            stepLog = new AssemblyStepLog();
            stepLog.setTaskId(task.getTaskId());
            stepLog.setStepId(stepId);
            stepLog.setStepNo(step.getStepOrder());
            stepLog.setSerialNumber(task.getDeviceSn());
            stepLog.setModelCode(task.getModelCode());
            stepLog.setAssemblyRound(task.getAssemblyRound());
            stepLog.setProcessStage(step.getProcessStage());
            stepLog.setWorkerName(task.getWorkerName());
        }

        // 4. 状态校验并更新
        String currentStepStatus = stepLog.getStatus() != null ? stepLog.getStatus() : STEP_WAITING;
        stateMachine.checkStepTransition(currentStepStatus, EVENT_START_STEP);
        stepLog.setStatus(STEP_RUNNING);
        stepLog.setStartTime(LocalDateTime.now());
        stepLog.setPassType(null);
        stepLog.setDuration(null);
        stepLog.setEndTime(null);

        if (stepLog.getId() == null) {
            assemblyStepLogMapper.insert(stepLog);
        } else {
            assemblyStepLogMapper.updateById(stepLog);
        }

        // 5. 下发配置到视觉服务并启动录像
        SopStep nextStep = findNextStep(task.getModelCode(), step.getStepOrder());
        List<String> historyTargets = findHistoryPassedTargets(task.getTaskId());

        VisionStepConfigCommand configCommand = VisionStepConfigCommand.builder()
                .deviceSn(task.getDeviceSn())
                .stepId(stepId)
                .target(step.getTargetLabel() != null ? step.getTargetLabel() : step.getDetectTarget())
                .nextTarget(nextStep != null
                        ? (nextStep.getTargetLabel() != null ? nextStep.getTargetLabel() : nextStep.getDetectTarget())
                        : null)
                .modelCode(task.getModelCode())
                .historyTargets(historyTargets)
                .time1(step.getStandardDuration() != null ? step.getStandardDuration() : 30)
                .time2(step.getStandardDuration() != null ? step.getStandardDuration() * 2 : 60)
                .build();
        visionGateway.updateStepConfig(configCommand);

        StartRecordCommand recordCommand = StartRecordCommand.builder()
                .deviceSn(task.getDeviceSn())
                .stepId(stepId)
                .taskId(task.getTaskId())
                .stepRunId(stepLog.getId())
                .build();
       VisionResult recordResult = visionGateway.startRecord(recordCommand);

        log.info("启动步骤成功: taskId={}, stepId={}, stepLogId={}", task.getTaskId(), stepId, stepLog.getId());

        processEventTxService.recordStepStarted(task.getTaskId(), task.getDeviceSn(), stepId, stepLog.getId(), step.getStepOrder());

        StartStepResultVO vo = new StartStepResultVO();
        vo.setTaskId(task.getTaskId());
        vo.setStepLogId(stepLog.getId());
        vo.setStepId(stepId);
        vo.setStepNo(step.getStepOrder());
        vo.setStepName(step.getStepTitle());
        vo.setStatus(stepLog.getStatus());
        vo.setTargetLabel(step.getTargetLabel() != null ? step.getTargetLabel() : step.getDetectTarget());
        vo.setNextTargetLabel(nextStep != null
                ? (nextStep.getTargetLabel() != null ? nextStep.getTargetLabel() : nextStep.getDetectTarget())
                : null);
        vo.setVideoPath(recordResult.getData() != null ? recordResult.getData().toString() : null);
        return vo;
    }

    // ==================== AI 自动过站回调 ====================

    @Override
    @Transactional
    public AiNextStepResultVO handleAiNextStep(AiNextStepCallbackCommand command) {
        // ═══ 阶段1a：基础校验（锁前，无 DB 查询） ═══
        if (!"AUTO_NEXT".equals(command.getAction())) {
            String payloadJson = toJson(command);
            rejectCallback(null, command.getDeviceSn(), null, null, null,
                    "回调 action 无效: " + command.getAction(), payloadJson);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "回调 action 无效: " + command.getAction());
        }
        if (!"0".equals(command.getStatus())) {
            String payloadJson = toJson(command);
            rejectCallback(null, command.getDeviceSn(), null, null, null,
                    "回调 status 非成功: " + command.getStatus(), payloadJson);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "回调 status 非成功: " + command.getStatus());
        }

        String deviceSn = command.getDeviceSn();
        if (!deviceLock.tryLock(deviceSn)) {
            throw new BusinessException(ErrorCode.SERVICE_UNAVAILABLE,
                    "设备 " + deviceSn + " 正在处理中，请稍后重试");
        }
        try {
            return doHandleAiNextStep(command);
        } finally {
            deviceLock.unlock(deviceSn);
        }
    }

    private AiNextStepResultVO doHandleAiNextStep(AiNextStepCallbackCommand command) {
        String payloadJson = toJson(command);
        String deviceSn = command.getDeviceSn();

        // ═══ 阶段1：新格式 eventId → 优先幂等检查（不依赖 RUNNING stepLog） ═══
        if (command.getEventId() != null && !command.getEventId().isEmpty()) {
            String eventId = command.getEventId();
            ProcessEvent existing = processEventService.getByEventId(eventId);

            if (existing != null) {
                if ("SUCCESS".equals(existing.getStatus())) {
                    processEventTxService.updateDuplicate(eventId);
                    log.info("回调幂等返回(SUCCESS): eventId={}", eventId);
                    return buildIdempotentResult(command.getTaskId(), deviceSn);
                }
                if ("PROCESSING".equals(existing.getStatus())) {
                    processEventTxService.updateDuplicate(eventId);
                    log.info("回调幂等返回(PROCESSING): eventId={}", eventId);
                    return buildIdempotentResult(command.getTaskId(), deviceSn);
                }
                if ("FAILED".equals(existing.getStatus())) {
                    processEventTxService.markProcessing(eventId);
                    processEventTxService.updateDuplicate(eventId);
                    log.info("FAILED事件重试: eventId={}", eventId);
                }
                // RECEIVED / IGNORED → continue to strict execution
            }

            // 新事件或 FAILED 重试 → 进入严格校验 + 执行
            return executeAiNextStep(command, deviceSn, eventId, payloadJson);
        }

        // ═══ 阶段1b：旧格式无 eventId → 先定位 RUNNING stepLog 再生成 LEGACY eventId ═══
        return executeAiNextStepLegacy(command, deviceSn, payloadJson);
    }

    /**
     * 新格式回调：有 eventId，先做幂等拦截后再进入严格上下文校验
     */
    private AiNextStepResultVO executeAiNextStep(AiNextStepCallbackCommand command,
                                                  String deviceSn, String eventId, String payloadJson) {
        // 严格上下文校验：定位 RUNNING 任务
        AssemblyTask task = locateRunningTask(command, deviceSn, payloadJson);

        // 严格上下文校验：定位 RUNNING 步骤日志
        AssemblyStepLog stepLog = locateRunningStepLog(command, task, payloadJson);

        // tryCreateProcessing（幂等窗口保护）
        ProcessEvent existing = processEventService.getByEventId(eventId);
        if (existing == null) {
            processEventTxService.tryCreateProcessing(eventId, task.getTaskId(),
                    deviceSn, stepLog.getStepId(), stepLog.getId(), "AI_PASS_RECEIVED", payloadJson);
        }

        return executeStepTransition(task, stepLog, eventId, deviceSn);
    }

    /**
     * 旧格式回调：无 eventId，先定位 RUNNING stepLog 生成 LEGACY eventId 后做幂等
     */
    private AiNextStepResultVO executeAiNextStepLegacy(AiNextStepCallbackCommand command,
                                                        String deviceSn, String payloadJson) {
        // 严格上下文校验：定位 RUNNING 任务
        AssemblyTask task = locateRunningTask(command, deviceSn, payloadJson);

        // 严格上下文校验：定位 RUNNING 步骤日志
        AssemblyStepLog stepLog = locateRunningStepLog(command, task, payloadJson);

        // 生成 LEGACY eventId
        String effectiveEventId = "LEGACY_AI_PASS:" + task.getDeviceSn() + ":" + task.getTaskId() + ":"
                + stepLog.getId() + ":AUTO_NEXT";

        ProcessEvent existing = processEventService.getByEventId(effectiveEventId);
        if (existing != null) {
            if ("SUCCESS".equals(existing.getStatus())) {
                processEventTxService.updateDuplicate(effectiveEventId);
                log.info("回调幂等返回(SUCCESS,legacy): eventId={}", effectiveEventId);
                return buildIdempotentResult(task);
            }
            if ("PROCESSING".equals(existing.getStatus())) {
                processEventTxService.updateDuplicate(effectiveEventId);
                log.info("回调幂等返回(PROCESSING,legacy): eventId={}", effectiveEventId);
                return buildIdempotentResult(task);
            }
            if ("FAILED".equals(existing.getStatus())) {
                processEventTxService.markProcessing(effectiveEventId);
                processEventTxService.updateDuplicate(effectiveEventId);
                log.info("FAILED事件重试(legacy): eventId={}", effectiveEventId);
            }
        } else {
            processEventTxService.tryCreateProcessing(effectiveEventId, task.getTaskId(),
                    deviceSn, stepLog.getStepId(), stepLog.getId(), "AI_PASS_RECEIVED", payloadJson);
        }

        return executeStepTransition(task, stepLog, effectiveEventId, deviceSn);
    }

    /**
     * 定位 RUNNING 状态的任务（严格校验）
     */
    private AssemblyTask locateRunningTask(AiNextStepCallbackCommand command, String deviceSn, String payloadJson) {
        AssemblyTask task;
        if (command.getTaskId() != null) {
            task = assemblyTaskMapper.selectById(command.getTaskId());
            if (task == null || task.getDeleted() == 1) {
                rejectCallback(null, deviceSn, command.getTaskId(), null, null,
                        "装配任务不存在", payloadJson);
                throw new BusinessException(ErrorCode.NOT_FOUND, "装配任务不存在");
            }
            if (!deviceSn.equals(task.getDeviceSn())) {
                rejectCallback(null, deviceSn, task.getTaskId(), null, null,
                        "回调 taskId 与 deviceSn 不匹配", payloadJson);
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                        "回调 taskId=" + command.getTaskId() + " 与 deviceSn=" + deviceSn + " 不匹配");
            }
        } else {
            task = assemblyTaskMapper.selectOne(
                    new LambdaQueryWrapper<AssemblyTask>()
                            .eq(AssemblyTask::getDeviceSn, deviceSn)
                            .eq(AssemblyTask::getStatus, TASK_RUNNING)
                            .eq(AssemblyTask::getDeleted, 0));
            if (task == null) {
                rejectCallback(null, deviceSn, null, null, null,
                        "设备 " + deviceSn + " 无进行中的装配任务", payloadJson);
                throw new BusinessException(ErrorCode.TASK_NOT_RUNNING,
                        "设备 " + deviceSn + " 无进行中的装配任务");
            }
        }

        if (!TASK_RUNNING.equals(task.getStatus())) {
            rejectCallback(null, deviceSn, task.getTaskId(), null, null,
                    "任务状态不是RUNNING: " + task.getStatus(), payloadJson);
            throw new BusinessException(ErrorCode.TASK_NOT_RUNNING,
                    "任务状态不是进行中，无法处理过站回调");
        }
        return task;
    }

    /**
     * 定位 RUNNING 状态的步骤日志（严格校验）
     */
    private AssemblyStepLog locateRunningStepLog(AiNextStepCallbackCommand command,
                                                  AssemblyTask task, String payloadJson) {
        String deviceSn = task.getDeviceSn();
        AssemblyStepLog stepLog;
        if (command.getStepRunId() != null) {
            stepLog = assemblyStepLogMapper.selectById(command.getStepRunId());
        } else {
            stepLog = assemblyStepLogMapper.selectOne(
                    new LambdaQueryWrapper<AssemblyStepLog>()
                            .eq(AssemblyStepLog::getTaskId, task.getTaskId())
                            .eq(AssemblyStepLog::getStatus, STEP_RUNNING)
                            .eq(AssemblyStepLog::getDeleted, 0));
        }
        if (stepLog == null) {
            rejectCallback(null, deviceSn, task.getTaskId(), command.getStepId(), command.getStepRunId(),
                    "无进行中的步骤日志", payloadJson);
            throw new BusinessException(ErrorCode.STEP_STATUS_DENIED, "无进行中的步骤日志");
        }

        if (!STEP_RUNNING.equals(stepLog.getStatus())) {
            rejectCallback(null, deviceSn, task.getTaskId(), stepLog.getStepId(), stepLog.getId(),
                    "步骤日志状态不是RUNNING: " + stepLog.getStatus(), payloadJson);
            throw new BusinessException(ErrorCode.STEP_STATUS_DENIED,
                    "当前步骤状态不允许过站: " + stepLog.getStatus());
        }
        if (!stepLog.getTaskId().equals(task.getTaskId())) {
            rejectCallback(null, deviceSn, task.getTaskId(), stepLog.getStepId(), stepLog.getId(),
                    "stepLog.taskId 与 task.taskId 不匹配", payloadJson);
            throw new BusinessException(ErrorCode.STEP_NOT_MATCH, "步骤日志不属于当前任务");
        }
        if (command.getStepId() != null && !command.getStepId().equals(stepLog.getStepId())) {
            rejectCallback(null, deviceSn, task.getTaskId(), command.getStepId(), stepLog.getId(),
                    "回调 stepId 与当前执行步骤不匹配", payloadJson);
            throw new BusinessException(ErrorCode.STEP_NOT_MATCH,
                    "回调 stepId=" + command.getStepId() + " 与执行中 stepId=" + stepLog.getStepId() + " 不匹配");
        }
        return stepLog;
    }

    /**
     * 执行步骤状态流转（过站核心逻辑）
     */
    private AiNextStepResultVO executeStepTransition(AssemblyTask task, AssemblyStepLog stepLog,
                                                      String effectiveEventId, String deviceSn) {
        LocalDateTime now = null;
        try {
            stateMachine.checkAiPassAllowed(stepLog.getStatus());
            stateMachine.checkStepTransition(STEP_RUNNING, EVENT_AI_PASS);

            now = LocalDateTime.now();
            stepLog.setStatus(STEP_AI_PASSED);
            stepLog.setPassType("AI_PASS");
            stepLog.setEndTime(now);
            if (stepLog.getStartTime() != null) {
                stepLog.setDuration(Duration.between(stepLog.getStartTime(), now).getSeconds());
            }
            assemblyStepLogMapper.updateById(stepLog);

            // 记录 STEP_FINISHED 事件（派生 eventId）
            String stepFinishedEventId = effectiveEventId + ":STEP_FINISHED:" + stepLog.getId();
            processEventTxService.recordEvent(stepFinishedEventId, task.getTaskId(), deviceSn,
                    stepLog.getStepId(), stepLog.getId(), "STEP_FINISHED", "SUCCESS",
                    "步骤 AI_PASSED: stepOrder=" + stepLog.getStepNo());

            // 停止录像
            StopRecordCommand stopCommand = StopRecordCommand.builder()
                    .deviceSn(task.getDeviceSn())
                    .taskId(task.getTaskId())
                    .stepRunId(stepLog.getId())
                    .build();
            visionGateway.stopRecord(stopCommand);

            // 判断下一步
            SopStep nextStep = findNextStep(task.getModelCode(), stepLog.getStepNo());
            AiNextStepResultVO vo = new AiNextStepResultVO();
            vo.setTaskId(task.getTaskId());
            vo.setDeviceSn(task.getDeviceSn());
            vo.setCompletedStepLogId(stepLog.getId());

            if (nextStep != null) {
                task.setCurrentStepIndex(nextStep.getStepOrder());
                assemblyTaskMapper.updateById(task);
                startNextStep(task, nextStep);

                vo.setTaskStatus(task.getStatus());
                vo.setCurrentStepNo(nextStep.getStepOrder());
                vo.setHasNextStep(true);
                vo.setNextStepId(nextStep.getStepId());
                vo.setNextStepName(nextStep.getStepTitle());
                log.info("AI过站自动进入下一步: taskId={}, currentStep={}, nextStepId={}",
                        task.getTaskId(), nextStep.getStepOrder(), nextStep.getStepId());
            } else {
                stateMachine.checkTaskTransition(TASK_RUNNING, EVENT_FINISH_TASK);
                task.setStatus(TASK_COMPLETED);
                task.setFinishTime(now);
                assemblyTaskMapper.updateById(task);

                // 记录 TASK_COMPLETED 事件（派生 eventId）
                String taskCompletedEventId = effectiveEventId + ":TASK_COMPLETED:" + task.getTaskId();
                processEventTxService.recordEvent(taskCompletedEventId, task.getTaskId(), deviceSn,
                        null, null, "TASK_COMPLETED", "SUCCESS",
                        "全部步骤完成");

                vo.setTaskStatus(TASK_COMPLETED);
                vo.setCurrentStepNo(task.getCurrentStepIndex());
                vo.setHasNextStep(false);
                log.info("AI过站全部步骤完成: taskId={}", task.getTaskId());
            }

            processEventTxService.markSuccess(effectiveEventId, vo.getTaskStatus());
            return vo;
        } catch (BusinessException e) {
            processEventTxService.markFailed(effectiveEventId, e.getMessage());
            throw e;
        }
    }

    // ==================== 手动完成任务 ====================

    @Override
    @Transactional
    public FinishTaskResultVO finishTask(Long taskId, String reason) {
        // 1. 查询任务
        AssemblyTask task = assemblyTaskMapper.selectById(taskId);
        if (task == null || task.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "装配任务不存在");
        }

        String deviceSn = task.getDeviceSn();
        if (!deviceLock.tryLock(deviceSn)) {
            throw new BusinessException(ErrorCode.SERVICE_UNAVAILABLE,
                    "设备 " + deviceSn + " 正在处理中，请稍后重试");
        }
        try {
            return doFinishTask(task, reason);
        } finally {
            deviceLock.unlock(deviceSn);
        }
    }

    private FinishTaskResultVO doFinishTask(AssemblyTask task, String reason) {
        // 2. 校验状态
        stateMachine.checkFinishTaskAllowed(task.getStatus());
        stateMachine.checkTaskTransition(TASK_RUNNING, EVENT_FINISH_TASK);

        LocalDateTime now = LocalDateTime.now();

        // 3. 结束当前运行中的步骤（标记为手动通过）
        AssemblyStepLog runningLog = assemblyStepLogMapper.selectOne(
                new LambdaQueryWrapper<AssemblyStepLog>()
                        .eq(AssemblyStepLog::getTaskId, task.getTaskId())
                        .eq(AssemblyStepLog::getStatus, STEP_RUNNING)
                        .eq(AssemblyStepLog::getDeleted, 0));
        if (runningLog != null) {
            stateMachine.checkStepTransition(STEP_RUNNING, EVENT_MANUAL_PASS);
            runningLog.setStatus(STEP_MANUAL_PASSED);
            runningLog.setPassType("MANUAL_PASS");
            runningLog.setEndTime(now);
            if (runningLog.getStartTime() != null) {
                runningLog.setDuration(Duration.between(runningLog.getStartTime(), now).getSeconds());
            }
            assemblyStepLogMapper.updateById(runningLog);
        }

        // 4. 更新任务
        task.setStatus(TASK_COMPLETED);
        task.setFinishTime(now);
        assemblyTaskMapper.updateById(task);

        // 5. 生成质量追溯记录
        generateQualityTrace(task);

        // 6. 计算总耗时
        long totalDuration = 0;
        if (task.getStartTime() != null) {
            totalDuration = Duration.between(task.getStartTime(), now).getSeconds();
        }

        log.info("手动完成任务: taskId={}, reason={}", task.getTaskId(), reason);

        FinishTaskResultVO vo = new FinishTaskResultVO();
        vo.setTaskId(task.getTaskId());
        vo.setDeviceSn(task.getDeviceSn());
        vo.setStatus(task.getStatus());
        vo.setFinishTime(now);
        vo.setTotalDuration(totalDuration);
        return vo;
    }

    // ==================== 私有方法 ====================

    /**
     * 幂等返回（新格式）：按 taskId 或 deviceSn 宽松查找任务，不要求 RUNNING
     */
    private AiNextStepResultVO buildIdempotentResult(Long taskId, String deviceSn) {
        AssemblyTask task;
        if (taskId != null) {
            task = assemblyTaskMapper.selectById(taskId);
        } else {
            task = assemblyTaskMapper.selectOne(
                    new LambdaQueryWrapper<AssemblyTask>()
                            .eq(AssemblyTask::getDeviceSn, deviceSn)
                            .eq(AssemblyTask::getDeleted, 0)
                            .orderByDesc(AssemblyTask::getCreateTime)
                            .last("limit 1"));
        }
        if (task == null || task.getDeleted() == 1) {
            log.warn("幂等返回时未找到任务: taskId={}, deviceSn={}", taskId, deviceSn);
            AiNextStepResultVO vo = new AiNextStepResultVO();
            vo.setDeviceSn(deviceSn);
            vo.setHasNextStep(false);
            return vo;
        }
        return buildIdempotentResult(task);
    }

    /**
     * 幂等返回时构造 AiNextStepResultVO（不推进步骤）
     */
    private AiNextStepResultVO buildIdempotentResult(AssemblyTask task) {
        AiNextStepResultVO vo = new AiNextStepResultVO();
        vo.setTaskId(task.getTaskId());
        vo.setDeviceSn(task.getDeviceSn());
        vo.setTaskStatus(task.getStatus());
        vo.setCurrentStepNo(task.getCurrentStepIndex());
        SopStep next = findNextStep(task.getModelCode(), task.getCurrentStepIndex());
        vo.setHasNextStep(next != null);
        if (next != null) {
            vo.setNextStepId(next.getStepId());
            vo.setNextStepName(next.getStepTitle());
        }
        return vo;
    }

    /**
     * 记录 CALLBACK_REJECTED 事件并抛异常
     */
    private void rejectCallback(String eventId, String deviceSn, Long taskId,
                                Long stepId, Long stepRunId, String reason, String payload) {
        String rejectEventId = "CALLBACK_REJECTED:" + deviceSn + ":"
                + (taskId != null ? taskId : "null") + ":" + System.currentTimeMillis();
        processEventTxService.recordRejected(rejectEventId, deviceSn, taskId, stepId, stepRunId, reason);
    }

    /**
     * 对象转 JSON（用于事件 payload 落库）
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    /**
     * 自动启动下一步骤
     */
    private void startNextStep(AssemblyTask task, SopStep nextStep) {
        // 停止上一步录像已在 handleAiNextStep 中处理

        // 创建新步骤日志
        AssemblyStepLog newLog = new AssemblyStepLog();
        newLog.setTaskId(task.getTaskId());
        newLog.setStepId(nextStep.getStepId());
        newLog.setStepNo(nextStep.getStepOrder());
        newLog.setSerialNumber(task.getDeviceSn());
        newLog.setModelCode(task.getModelCode());
        newLog.setAssemblyRound(task.getAssemblyRound());
        newLog.setProcessStage(nextStep.getProcessStage());
        newLog.setWorkerName(task.getWorkerName());
        newLog.setStatus(STEP_RUNNING);
        newLog.setStartTime(LocalDateTime.now());
        assemblyStepLogMapper.insert(newLog);

        // 下发配置并启动录像
        SopStep stepAfterNext = findNextStep(task.getModelCode(), nextStep.getStepOrder());
        List<String> historyTargets = findHistoryPassedTargets(task.getTaskId());

        VisionStepConfigCommand configCommand = VisionStepConfigCommand.builder()
                .deviceSn(task.getDeviceSn())
                .stepId(nextStep.getStepId())
                .target(nextStep.getTargetLabel() != null ? nextStep.getTargetLabel() : nextStep.getDetectTarget())
                .nextTarget(stepAfterNext != null
                        ? (stepAfterNext.getTargetLabel() != null ? stepAfterNext.getTargetLabel() : stepAfterNext.getDetectTarget())
                        : null)
                .modelCode(task.getModelCode())
                .historyTargets(historyTargets)
                .time1(nextStep.getStandardDuration() != null ? nextStep.getStandardDuration() : 30)
                .time2(nextStep.getStandardDuration() != null ? nextStep.getStandardDuration() * 2 : 60)
                .build();
        visionGateway.updateStepConfig(configCommand);

        StartRecordCommand recordCommand = StartRecordCommand.builder()
                .deviceSn(task.getDeviceSn())
                .stepId(nextStep.getStepId())
                .taskId(task.getTaskId())
                .stepRunId(newLog.getId())
                .build();
        visionGateway.startRecord(recordCommand);
    }

    /**
     * 查找下一步骤
     */
    private SopStep findNextStep(String modelCode, Integer currentStepOrder) {
        List<SopStep> steps = sopStepMapper.selectList(
                new LambdaQueryWrapper<SopStep>()
                        .eq(SopStep::getModelCode, modelCode)
                        .eq(SopStep::getDeleted, 0)
                        .gt(SopStep::getStepOrder, currentStepOrder)
                        .orderByAsc(SopStep::getStepOrder)
                        .last("limit 1"));
        return steps.isEmpty() ? null : steps.get(0);
    }

    /**
     * 查询已完成的检测目标历史
     */
    private List<String> findHistoryPassedTargets(Long taskId) {
        List<AssemblyStepLog> passedLogs = assemblyStepLogMapper.selectList(
                new LambdaQueryWrapper<AssemblyStepLog>()
                        .eq(AssemblyStepLog::getTaskId, taskId)
                        .eq(AssemblyStepLog::getDeleted, 0)
                        .in(AssemblyStepLog::getStatus, STEP_AI_PASSED, STEP_MANUAL_PASSED)
                        .orderByAsc(AssemblyStepLog::getStepNo));
        if (passedLogs.isEmpty()) {
            return Collections.emptyList();
        }
        return passedLogs.stream().map(log -> {
            SopStep step = sopStepMapper.selectById(log.getStepId());
            if (step != null) {
                return step.getTargetLabel() != null ? step.getTargetLabel() : step.getDetectTarget();
            }
            return "";
        }).filter(s -> s != null && !s.isEmpty()).collect(Collectors.toList());
    }

    /**
     * 生成基础质量追溯记录
     */
    private void generateQualityTrace(AssemblyTask task) {
        List<AssemblyStepLog> allLogs = assemblyStepLogMapper.selectList(
                new LambdaQueryWrapper<AssemblyStepLog>()
                        .eq(AssemblyStepLog::getTaskId, task.getTaskId())
                        .eq(AssemblyStepLog::getDeleted, 0));

        int totalSteps = allLogs.size();
        int passedSteps = (int) allLogs.stream()
                .filter(l -> STEP_AI_PASSED.equals(l.getStatus())).count();
        int manualSteps = (int) allLogs.stream()
                .filter(l -> STEP_MANUAL_PASSED.equals(l.getStatus())).count();
        int exceptionCount = (int) allLogs.stream()
                .filter(l -> STEP_FAILED.equals(l.getStatus())).count();

        long totalDuration = 0;
        if (task.getStartTime() != null && task.getFinishTime() != null) {
            totalDuration = Duration.between(task.getStartTime(), task.getFinishTime()).getSeconds();
        }

        String finalResult = exceptionCount > 0 ? "FAIL" : (manualSteps > 0 ? "MANUAL" : "PASS");

        QualityTrace trace = new QualityTrace();
        trace.setTaskId(task.getTaskId());
        trace.setDeviceSn(task.getDeviceSn());
        trace.setModelCode(task.getModelCode());
        trace.setWorkerId(task.getWorkerId());
        trace.setTotalSteps(totalSteps);
        trace.setPassedSteps(passedSteps);
        trace.setManualSteps(manualSteps);
        trace.setExceptionCount(exceptionCount);
        trace.setStartTime(task.getStartTime());
        trace.setFinishTime(task.getFinishTime());
        trace.setTotalDuration(totalDuration);
        trace.setFinalResult(finalResult);
        qualityTraceMapper.insert(trace);

        log.info("生成质量追溯记录: taskId={}, result={}, total={}, aiPass={}, manual={}, exception={}",
                task.getTaskId(), finalResult, totalSteps, passedSteps, manualSteps, exceptionCount);

        processEventTxService.recordQualityTraceCreated(task.getTaskId(), task.getDeviceSn(), finalResult);
    }
}
