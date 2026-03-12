package com.ruoyi.process.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.ruoyi.process.domain.BizAssemblyTask;

@Mapper
public interface BizAssemblyTaskMapper
{
    public BizAssemblyTask selectBizAssemblyTaskById(Long taskId);

    // 你的其他方法...
    public BizAssemblyTask selectBizAssemblyTaskBySn(String deviceSn);
    public List<BizAssemblyTask> selectBizAssemblyTaskList(BizAssemblyTask bizAssemblyTask);
    public int insertBizAssemblyTask(BizAssemblyTask bizAssemblyTask);
    public int updateBizAssemblyTask(BizAssemblyTask bizAssemblyTask);
    public int deleteBizAssemblyTaskById(Long taskId);
    public int deleteBizAssemblyTaskByIds(Long[] taskIds);
}
