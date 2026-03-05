package com.ruoyi.process.service;

import java.util.List;
import com.ruoyi.process.domain.BizAssemblyTask;
import com.ruoyi.process.domain.dto.SopStepActionRequest;

public interface IBizAssemblyTaskService
{
    Long startSopStep(SopStepActionRequest req);

    void stopSopStep(SopStepActionRequest req);

    BizAssemblyTask startTask(BizAssemblyTask taskParams);

    /**
     * 根据ID查询
     */
    public BizAssemblyTask selectBizAssemblyTaskById(Long taskId);

    /**
     * AI 专用：自动完成步骤并记录流水
     * @param deviceSn 设备编号
     * @return 是否成功
     */
    boolean completeStepByAi(String deviceSn);

    /**
     * 核心：检查设备状态 (Controller里调用的就是这个)
     */
    public BizAssemblyTask checkDeviceStatus(String deviceSn);

    /**
     * 查询列表
     */
    public List<BizAssemblyTask> selectBizAssemblyTaskList(BizAssemblyTask bizAssemblyTask);

    /**
     * 新增
     */
    public int insertBizAssemblyTask(BizAssemblyTask bizAssemblyTask);


    /**
     * 修改
     */
    public int updateBizAssemblyTask(BizAssemblyTask bizAssemblyTask);
}
