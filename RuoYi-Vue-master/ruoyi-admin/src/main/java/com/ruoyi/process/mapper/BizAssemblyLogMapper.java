package com.ruoyi.process.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.ruoyi.process.domain.BizAssemblyLog;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BizAssemblyLogMapper
{   BizAssemblyLog selectLastLog(@Param("serialNumber") String serialNumber,
                                 @Param("stepId") Long stepId,
                                 @Param("assemblyRound") Integer assemblyRound);
    public BizAssemblyLog selectBizAssemblyLogById(Long logId);

    public List<BizAssemblyLog> selectBizAssemblyLogList(BizAssemblyLog bizAssemblyLog);

    public int insertBizAssemblyLog(BizAssemblyLog bizAssemblyLog);

    public int updateBizAssemblyLog(BizAssemblyLog bizAssemblyLog);

    public int deleteBizAssemblyLogById(Long logId);

    public int deleteBizAssemblyLogByIds(Long[] logIds);

    BizAssemblyLog selectInProgressLog(@Param("deviceSn") String deviceSn, @Param("stepId") Long stepId, @Param("round") Integer round);
}
