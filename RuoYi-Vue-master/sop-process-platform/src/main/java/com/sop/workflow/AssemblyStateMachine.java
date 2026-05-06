package com.sop.workflow;

import com.sop.common.exception.BusinessException;
import com.sop.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 装配状态机
 * <p>
 * 轻量级状态机，负责校验任务和步骤的状态流转合法性，不做持久化。
 *
 * @author SOP Team
 */
@Component
public class AssemblyStateMachine {

    // ==================== 任务状态 ====================

    public static final String TASK_WAITING_START = "WAITING_START";
    public static final String TASK_RUNNING = "RUNNING";
    public static final String TASK_EXCEPTION = "EXCEPTION";
    public static final String TASK_COMPLETED = "COMPLETED";
    public static final String TASK_WAREHOUSED = "WAREHOUSED";

    // ==================== 步骤状态 ====================

    public static final String STEP_WAITING = "WAITING";
    public static final String STEP_RUNNING = "RUNNING";
    public static final String STEP_AI_PASSED = "AI_PASSED";
    public static final String STEP_MANUAL_PASSED = "MANUAL_PASSED";
    public static final String STEP_FAILED = "FAILED";
    public static final String STEP_SKIPPED = "SKIPPED";

    // ==================== 事件 ====================

    public static final String EVENT_START_TASK = "START_TASK";
    public static final String EVENT_START_STEP = "START_STEP";
    public static final String EVENT_AI_PASS = "AI_PASS";
    public static final String EVENT_MANUAL_PASS = "MANUAL_PASS";
    public static final String EVENT_STEP_EXCEPTION = "STEP_EXCEPTION";
    public static final String EVENT_FINISH_TASK = "FINISH_TASK";

    // ==================== 任务状态流转表 ====================

    private static final Map<String, Set<String>> TASK_TRANSITIONS = Map.of(
            TASK_WAITING_START, Set.of(TASK_RUNNING),
            TASK_RUNNING, Set.of(TASK_EXCEPTION, TASK_COMPLETED),
            TASK_EXCEPTION, Set.of(TASK_RUNNING),
            TASK_COMPLETED, Set.of(TASK_WAREHOUSED)
    );

    private static final Map<String, String> TASK_EVENT_TO_NEXT_STATUS = Map.of(
            EVENT_START_TASK, TASK_RUNNING,
            EVENT_FINISH_TASK, TASK_COMPLETED,
            EVENT_STEP_EXCEPTION, TASK_EXCEPTION
    );

    // ==================== 步骤状态流转表 ====================

    private static final Map<String, Set<String>> STEP_TRANSITIONS = Map.of(
            STEP_WAITING, Set.of(STEP_RUNNING, STEP_SKIPPED),
            STEP_RUNNING, Set.of(STEP_AI_PASSED, STEP_MANUAL_PASSED, STEP_FAILED)
    );

    private static final Map<String, String> STEP_EVENT_TO_NEXT_STATUS = Map.of(
            EVENT_START_STEP, STEP_RUNNING,
            EVENT_AI_PASS, STEP_AI_PASSED,
            EVENT_MANUAL_PASS, STEP_MANUAL_PASSED,
            EVENT_STEP_EXCEPTION, STEP_FAILED
    );

    // ==================== 校验方法 ====================

    /**
     * 校验任务状态流转是否合法
     *
     * @param currentStatus 当前任务状态
     * @param event         触发事件
     */
    public void checkTaskTransition(String currentStatus, String event) {
        String nextStatus = nextTaskStatus(currentStatus, event);
        Set<String> allowed = TASK_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(nextStatus)) {
            throw new BusinessException(ErrorCode.STATUS_CONFLICT,
                    "任务状态流转非法: " + currentStatus + " ->(" + event + ")-> " + nextStatus);
        }
    }

    /**
     * 校验步骤状态流转是否合法
     *
     * @param currentStatus 当前步骤状态
     * @param event         触发事件
     */
    public void checkStepTransition(String currentStatus, String event) {
        String nextStatus = nextStepStatus(currentStatus, event);
        Set<String> allowed = STEP_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(nextStatus)) {
            throw new BusinessException(ErrorCode.STEP_STATUS_DENIED,
                    "步骤状态流转非法: " + currentStatus + " ->(" + event + ")-> " + nextStatus);
        }
    }

    /**
     * 获取事件对应的下一个任务状态
     *
     * @param currentStatus 当前任务状态
     * @param event         触发事件
     */
    public String nextTaskStatus(String currentStatus, String event) {
        String next = TASK_EVENT_TO_NEXT_STATUS.get(event);
        if (next == null) {
            throw new BusinessException(ErrorCode.STATUS_CONFLICT,
                    "未知任务事件: " + event);
        }
        return next;
    }

    /**
     * 获取事件对应的下一个步骤状态
     *
     * @param currentStatus 当前步骤状态
     * @param event         触发事件
     */
    public String nextStepStatus(String currentStatus, String event) {
        String next = STEP_EVENT_TO_NEXT_STATUS.get(event);
        if (next == null) {
            throw new BusinessException(ErrorCode.STEP_STATUS_DENIED,
                    "未知步骤事件: " + event);
        }
        return next;
    }

    /**
     * 判断任务是否为终态
     */
    public boolean isTaskFinal(String status) {
        return TASK_COMPLETED.equals(status) || TASK_WAREHOUSED.equals(status);
    }

    /**
     * 判断步骤是否为终态
     */
    public boolean isStepFinal(String status) {
        return STEP_AI_PASSED.equals(status) || STEP_MANUAL_PASSED.equals(status)
                || STEP_FAILED.equals(status) || STEP_SKIPPED.equals(status);
    }

    /**
     * 校验当前步骤是否允许 AI_PASS 事件
     */
    public void checkAiPassAllowed(String stepStatus) {
        if (!STEP_RUNNING.equals(stepStatus)) {
            throw new BusinessException(ErrorCode.STEP_STATUS_DENIED,
                    "当前步骤状态 " + stepStatus + " 不允许 AI 自动过站");
        }
    }

    /**
     * 校验任务是否允许 FINISH_TASK 事件
     */
    public void checkFinishTaskAllowed(String taskStatus) {
        if (!TASK_RUNNING.equals(taskStatus)) {
            throw new BusinessException(ErrorCode.STATUS_CONFLICT,
                    "当前任务状态 " + taskStatus + " 不允许手动完成");
        }
    }
}
