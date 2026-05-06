package com.sop.process.service;

import com.sop.process.command.*;
import com.sop.process.vo.*;

/**
 * SOP 装配执行服务接口
 * <p>
 * 负责工业 SOP 装配核心流程控制：开工 → 启动步骤 → AI过站 → 完成任务。
 *
 * @author SOP Team
 */
public interface SopExecutionService {

    /**
     * 开工：根据设备SN创建装配任务，状态置为RUNNING
     */
    StartTaskResultVO startTask(StartTaskCommand command);

    /**
     * 启动步骤：启动指定任务的指定步骤
     */
    StartStepResultVO startStep(Long taskId, Long stepId);

    /**
     * AI 自动过站回调：Python视觉服务完成检测后回调
     */
    AiNextStepResultVO handleAiNextStep(AiNextStepCallbackCommand command);

    /**
     * 手动完成任务
     */
    FinishTaskResultVO finishTask(Long taskId, String reason);
}
