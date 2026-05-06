package com.ruoyi.process.service.impl;

import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.process.domain.BizAssemblyLog;
import com.ruoyi.process.domain.BizAssemblyTask;
import com.ruoyi.process.domain.BizWorker;
import com.ruoyi.process.domain.BizSopStep;
import com.ruoyi.process.domain.dto.SopStepActionRequest;
import com.ruoyi.process.mapper.BizAssemblyLogMapper;
import com.ruoyi.process.mapper.BizAssemblyTaskMapper;
import com.ruoyi.process.mapper.BizSopStepMapper;
import com.ruoyi.process.service.IBizAssemblyTaskService;
import com.ruoyi.process.service.IBizWorkerService;
import com.ruoyi.process.service.PythonInteractionService;

/**
 * 装配任务 Service 业务层处理
 */
@Service
public class BizAssemblyTaskServiceImpl implements IBizAssemblyTaskService
{
    @Autowired
    private BizAssemblyTaskMapper bizAssemblyTaskMapper;

    @Autowired
    private IBizWorkerService bizWorkerService;

    @Autowired
    private BizAssemblyLogMapper bizAssemblyLogMapper;

    @Autowired
    private PythonInteractionService pythonService;

    @Autowired
    private BizSopStepMapper bizSopStepMapper;

    //  开启步骤 / 归档
    @Override
    @Transactional
    public Long startSopStep(SopStepActionRequest req) {
        String deviceSn = req.getDeviceSn();
        Long stepId = req.getStepId() != null ? req.getStepId() : 0L;
        Integer round = req.getAssemblyRound();
        if (round == null) round = 1;

        boolean isDirectFinish = "2".equals(req.getStatus());

        // 1. 补全 ModelCode
        if (StringUtils.isEmpty(req.getModelCode())) {
            BizAssemblyTask task = bizAssemblyTaskMapper.selectBizAssemblyTaskBySn(deviceSn);
            if (task != null) req.setModelCode(task.getModelCode());
        }

        // 2. 通知 Python (仅在非归档时)
        if (!isDirectFinish) {
            // 必须确保拿到 stepOrder 才能找下一关
            Integer stepOrder = req.getStepOrder();
            String currentTarget = req.getTarget();

            // 如果前端没传 Order，或者没传 Target，强制查库补全
            if (stepOrder == null || StringUtils.isEmpty(currentTarget)) {
                BizSopStep currentStep = bizSopStepMapper.selectBizSopStepByStepId(stepId);
                if (currentStep != null) {
                    stepOrder = currentStep.getStepOrder().intValue();
                    req.setStepOrder(stepOrder); // 补全 Order
                    if (StringUtils.isEmpty(currentTarget)) {
                        req.setTarget(currentStep.getDetectTarget()); // 补全 Target
                    }
                    System.out.println("🔧 [补全] 查库补全 Order=" + stepOrder + ", Target=" + req.getTarget());
                }
            }

            // 查找并注入 "第二关" (Next Target)
            if (stepOrder != null && StringUtils.isNotEmpty(req.getModelCode())) {
                String nextTarget = findTargetByOrder(req.getModelCode(), stepOrder + 1);

                if (StringUtils.isNotEmpty(nextTarget)) {
                    req.setNextTarget(nextTarget);
                    System.out.println("[双重判定] 成功配置: 第1关[" + req.getTarget() + "] -> 第2关[" + nextTarget + "]");
                } else {
                    req.setNextTarget(null);
                    System.out.println("[单重判定] 仅检测当前目标: " + req.getTarget());
                }
            } else {
                System.err.println(" [警告] 无法获取 StepOrder 或 ModelCode，双重判定失效！");
            }

            // 启动录像
            long nameIdentifier = (stepOrder != null) ? stepOrder.longValue() : stepId;
            String videoName = String.format("%s_step%d_%d.mp4", deviceSn, nameIdentifier, System.currentTimeMillis());
            pythonService.startStepRecording(req, videoName);
        }

        // 3. 幂等检查 (复用逻辑)
        if (!isDirectFinish) {
            BizAssemblyLog existingLog = bizAssemblyLogMapper.selectLastLog(deviceSn, null, round);
            if (existingLog != null && "1".equals(existingLog.getStatus()) && existingLog.getStepId().equals(stepId)) {
                return existingLog.getId();
            }
        }

        // 4. 创建新日志
        BizAssemblyLog log = new BizAssemblyLog();
        log.setSerialNumber(deviceSn);
        log.setStepId(stepId);
        log.setAssemblyRound(round);
        log.setModelCode(req.getModelCode());

        String stageSuffix = (round == 1) ? " (首次装配)" : " (第" + (round - 1) + "次返修)";
        log.setProcessStage(req.getStepName() + stageSuffix);
        log.setStartTime(new Date());

        if (StringUtils.isNotEmpty(req.getWorkerName())) {
            log.setWorkerName(req.getWorkerName());
        } else {
            BizAssemblyTask task = bizAssemblyTaskMapper.selectBizAssemblyTaskBySn(deviceSn);
            if (task != null) log.setWorkerName(task.getWorkerName());
        }

        if (isDirectFinish) {
            log.setStatus("2");
            log.setEndTime(new Date());
        } else {
            log.setStatus("1");
        }

        bizAssemblyLogMapper.insertBizAssemblyLog(log);
        return log.getId();
    }

    @Override
    @Transactional
    public void stopSopStep(SopStepActionRequest req) {
        Long logId = req.getId();
        if (logId == null) return;

        BizAssemblyLog log = bizAssemblyLogMapper.selectBizAssemblyLogById(logId);
        if (log == null) return;

        String videoFile = pythonService.stopRecording();
        log.setEndTime(new Date());

        if (StringUtils.isNotEmpty(req.getStatus())) {
            log.setStatus(req.getStatus());
        } else {
            log.setStatus("2"); // 默认完成
        }

        if (StringUtils.isNotEmpty(videoFile)) {
            log.setVideoUrl("/videos/" + videoFile);
        }

        bizAssemblyLogMapper.updateBizAssemblyLog(log);
    }

    //  3. AI 自动流转 (修复日志查找 + 双重判定)
    @Override
    @Transactional
    public boolean completeStepByAi(String deviceSn) {
        // 最外层包裹 try-catch
        try {
            System.out.println("========== [AI过站逻辑启动] SN: " + deviceSn + " ==========");

            // 1. 检查任务状态
            BizAssemblyTask task = bizAssemblyTaskMapper.selectBizAssemblyTaskBySn(deviceSn);
            if (task == null) {
                System.err.println("严重错误：找不到 SN=" + deviceSn + " 的任务！");
                return false;
            }
            if (!"0".equals(task.getStatus())) {
                System.err.println("任务未开始或已结束，状态: " + task.getStatus());
                return false;
            }

            // ---------------------------------------------------------
            // A. 暴力闭环：找到最新的一条“进行中”日志，强制填入结束时间
            // ---------------------------------------------------------
            String videoFileName = null;
            try {
                videoFileName = pythonService.stopRecording();
            } catch (Exception e) {
                System.err.println("停止录像失败(不影响业务): " + e.getMessage());
            }

            // 构造宽泛查询
            BizAssemblyLog query = new BizAssemblyLog();
            query.setSerialNumber(deviceSn);
            query.setStatus("1");
            List<BizAssemblyLog> runningLogs = bizAssemblyLogMapper.selectBizAssemblyLogList(query);

            System.out.println("查到进行中日志条数: " + (runningLogs == null ? 0 : runningLogs.size()));

            if (runningLogs != null && !runningLogs.isEmpty()) {
                BizAssemblyLog lastRunningLog = runningLogs.get(runningLogs.size() - 1);

                System.out.println("锁定目标日志 ID: " + lastRunningLog.getId() + " | 步骤: " + lastRunningLog.getProcessStage());

                // 🔥 核心动作：只填入时间，状态保持 "1" (进行中)
                lastRunningLog.setEndTime(new Date());
                lastRunningLog.setProcessStage("AI自动检测通过");
                if (videoFileName != null) {
                    lastRunningLog.setVideoUrl("/videos/" + videoFileName);
                }

                // 状态逻辑：强制保持 "1"
                lastRunningLog.setStatus("1");

                // ⚡️ 立即执行更新
                bizAssemblyLogMapper.updateBizAssemblyLog(lastRunningLog);
                System.out.println(" 已更新结束时间 (状态保持进行中)");

                // 更新 list 中的对象
                runningLogs.set(runningLogs.size() - 1, lastRunningLog);
            } else {
                System.err.println("严重警告：数据库里没有该设备正在进行的日志！结束时间无法记录！");
            }

            // ---------------------------------------------------------
            // B. 推进任务索引
            // ---------------------------------------------------------
            String modelCode = task.getModelCode();
            BizSopStep stepQuery = new BizSopStep();
            stepQuery.setModelCode(modelCode);
            List<BizSopStep> allSteps = bizSopStepMapper.selectBizSopStepList(stepQuery);

            if (allSteps == null || allSteps.isEmpty()) {
                System.err.println(" 严重错误：找不到型号 " + modelCode + " 的SOP步骤配置！");
                return false;
            }
            int totalSteps = allSteps.size();

            // 【修复】防止空指针
            int currentIndex = (task.getCurrentStepIndex() == null) ? 1 : task.getCurrentStepIndex();
            int nextIndex = currentIndex + 1;

            System.out.println(" 进度: 当前第 " + currentIndex + " 步 -> 准备进入第 " + nextIndex + " 步 (总 " + totalSteps + " 步)");

            // 【核心修改区域】 如果刚才做的是最后一步
            if (currentIndex >= totalSteps) {
                // 用户要求：即使是最后一步，日志状态也保持 "1"，任务状态也保持 "0"
                // 所有的“完成”状态由人工点击“入库”触发

                // 这里不需要再把日志 update 成 "2" 了，因为 A 部分已经 update 过了
                System.out.println(" 最后一步AI检测通过，状态保持[进行中]，等待人工入库。");

                // 任务主状态：保持 "0" (进行中)
                task.setStatus("0");
                System.out.println(" AI流程全部结束，等待入库操作。");
            } else {
                task.setStatus("0"); // 继续进行
            }

            // 依然推进步骤索引（为了不再触发旧步骤的AI）
            task.setCurrentStepIndex(nextIndex);
            bizAssemblyTaskMapper.updateBizAssemblyTask(task);

            // ---------------------------------------------------------
// C. 开启下一步
// ---------------------------------------------------------
            if (nextIndex <= totalSteps) {
                try {
                    // 1. 找到下一步配置
                    BizSopStep nextSopStep = allSteps.stream()
                            .filter(s -> s.getStepOrder() != null && s.getStepOrder().equals(nextIndex))
                            .findFirst().orElse(null);

                    if (nextSopStep != null) {
                        SopStepActionRequest nextReq = new SopStepActionRequest();
                        nextReq.setDeviceSn(deviceSn);
                        nextReq.setModelCode(modelCode);
                        nextReq.setStepId(nextSopStep.getStepId());
                        nextReq.setStepOrder(nextSopStep.getStepOrder().intValue());
                        nextReq.setStepName(nextSopStep.getStepTitle());
                        nextReq.setTarget(nextSopStep.getDetectTarget());
                        nextReq.setAssemblyRound(task.getAssemblyRound());
                        nextReq.setWorkerName(task.getWorkerName());

                        // 逻辑：在所有步骤中，找到序号比下一步(nextIndex)小的步骤，把它们的目标取出来
                        if (allSteps != null && !allSteps.isEmpty()) {
                            List<String> historyList = allSteps.stream()
                                    // 1. 筛选：只找序号比当前“下一步”小的 (即以前做过的)
                                    .filter(s -> s.getStepOrder() != null && s.getStepOrder() < nextIndex)
                                    // 2. 提取：只取 detectTarget 字段
                                    .map(BizSopStep::getDetectTarget)
                                    // 3. 过滤：排除空的，或者 null 的
                                    .filter(target -> target != null && !target.isEmpty())
                                    // 4. 去重：防止有重复的零件
                                    .distinct()
                                    .collect(Collectors.toList());

                            // 放入请求体 (请确保 SopStepActionRequest 类里加了 List<String> historyTargets 字段)
                            nextReq.setHistoryTargets(historyList);

                            System.out.println(" [历史白名单] 下发给Python的历史零件: " + historyList);
                        }

                        String futureTarget = allSteps.stream()
                                .filter(s -> s.getStepOrder() != null && s.getStepOrder().equals(nextIndex + 1))
                                .map(BizSopStep::getDetectTarget)
                                .findFirst().orElse(null);
                        nextReq.setNextTarget(futureTarget);

                        String nextVideoName = String.format("%s_step%d_%d.mp4", deviceSn, nextIndex, System.currentTimeMillis());
                        pythonService.startStepRecording(nextReq, nextVideoName);

                        BizAssemblyLog nextLog = new BizAssemblyLog();
                        nextLog.setSerialNumber(deviceSn);
                        nextLog.setStepId(nextSopStep.getStepId());
                        nextLog.setAssemblyRound(task.getAssemblyRound());
                        nextLog.setModelCode(modelCode);
                        nextLog.setWorkerName(task.getWorkerName());
                        nextLog.setStartTime(new Date());
                        nextLog.setStatus("1"); // 新步骤也是进行中

                        String suffix = (task.getAssemblyRound() == 1) ? " (首次装配)" : " (返修)";
                        nextLog.setProcessStage(nextSopStep.getStepTitle() + suffix);

                        bizAssemblyLogMapper.insertBizAssemblyLog(nextLog);
                        System.out.println(" 下一步骤已开启: " + nextSopStep.getStepTitle());
                    } else {
                        System.err.println(" 警告：计算出下一步是 " + nextIndex + "，但在 SOP 表里没找到对应的配置！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(" 开启下一步失败，但上一步结束时间已保存。错误: " + e.getMessage());
                }
            }

            return true;

        } catch (Exception e) {
            // 强制打印堆栈
            System.err.println("！错误：");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // 辅助工具方法
    /**
     * 根据 Order 查找目标 (优化版)
     */
    private String findTargetByOrder(String modelCode, Integer targetOrder) {
        if (StringUtils.isEmpty(modelCode) || targetOrder == null) return null;

        BizSopStep query = new BizSopStep();
        query.setModelCode(modelCode);
        query.setStepOrder(targetOrder.longValue());
        List<BizSopStep> steps = bizSopStepMapper.selectBizSopStepList(query);

        if (steps != null && !steps.isEmpty()) {
            return steps.get(0).getDetectTarget();
        }
        return null;
    }

    // 4. 任务管理逻辑
    @Override
    @Transactional
    public BizAssemblyTask startTask(BizAssemblyTask taskParams)
    {
        if (taskParams.getWorkerId() == null) throw new ServiceException("必须选择作业人员");
        BizWorker worker = bizWorkerService.selectBizWorkerById(taskParams.getWorkerId());
        if (worker == null) throw new ServiceException("选择的工人不存在");
        String currentWorkerName = worker.getWorkerName();
        taskParams.setWorkerName(currentWorkerName);
        BizAssemblyTask latestTask = bizAssemblyTaskMapper.selectBizAssemblyTaskBySn(taskParams.getDeviceSn());

        if (latestTask == null) return createNewTask(taskParams, 1);

        if (!"2".equals(latestTask.getStatus())) {
            if (!latestTask.getWorkerId().equals(taskParams.getWorkerId())) {
                throw new ServiceException("该设备正由 [" + latestTask.getWorkerName() + "] 作业中，请切换该人员继续！");
            }
            latestTask.setStatus("0");
            latestTask.setWorkerName(currentWorkerName);
            if (StringUtils.isEmpty(latestTask.getModelCode()) && StringUtils.isNotEmpty(taskParams.getModelCode())) {
                latestTask.setModelCode(taskParams.getModelCode());
            }
            bizAssemblyTaskMapper.updateBizAssemblyTask(latestTask);
            return latestTask;
        }

        int lastRound = (latestTask.getAssemblyRound() == null) ? 1 : latestTask.getAssemblyRound();
        if (taskParams.getModelCode() == null) taskParams.setModelCode(latestTask.getModelCode());
        return createNewTask(taskParams, lastRound + 1);
    }

    private BizAssemblyTask createNewTask(BizAssemblyTask task, Integer round) {
        task.setCreateTime(DateUtils.getNowDate());
        task.setCurrentStepIndex(1);
        task.setStatus("0");
        task.setAssemblyRound(round);
        bizAssemblyTaskMapper.insertBizAssemblyTask(task);
        return task;
    }

    // 基础查询方法
    @Override public BizAssemblyTask selectBizAssemblyTaskById(Long taskId) { return bizAssemblyTaskMapper.selectBizAssemblyTaskById(taskId); }
    @Override public BizAssemblyTask checkDeviceStatus(String deviceSn) { return bizAssemblyTaskMapper.selectBizAssemblyTaskBySn(deviceSn); }
    @Override public List<BizAssemblyTask> selectBizAssemblyTaskList(BizAssemblyTask bizAssemblyTask) { return bizAssemblyTaskMapper.selectBizAssemblyTaskList(bizAssemblyTask); }
    @Override public int insertBizAssemblyTask(BizAssemblyTask bizAssemblyTask) {
        bizAssemblyTask.setCreateTime(DateUtils.getNowDate());
        if (bizAssemblyTask.getAssemblyRound() == null) bizAssemblyTask.setAssemblyRound(1);
        return bizAssemblyTaskMapper.insertBizAssemblyTask(bizAssemblyTask);
    }
    @Override public int updateBizAssemblyTask(BizAssemblyTask bizAssemblyTask) {
        bizAssemblyTask.setUpdateTime(DateUtils.getNowDate());
        return bizAssemblyTaskMapper.updateBizAssemblyTask(bizAssemblyTask);
    }
}