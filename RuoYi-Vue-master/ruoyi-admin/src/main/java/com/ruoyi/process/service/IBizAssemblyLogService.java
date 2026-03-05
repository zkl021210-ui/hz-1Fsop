package com.ruoyi.process.service;

import java.util.List;
import com.ruoyi.process.domain.BizAssemblyLog;

/**
 * 装配流水日志 Service 接口
 */
public interface IBizAssemblyLogService
{
    /**
     * 查询装配流水日志
     * * @param logId 装配流水日志主键
     * @return 装配流水日志
     */
    // 🔴 修复点：这里必须和实现类一样，叫 ById，不要叫 ByLogId
    public BizAssemblyLog selectBizAssemblyLogById(Long logId);

    /**
     * 查询装配流水日志列表
     * * @param bizAssemblyLog 装配流水日志
     * @return 装配流水日志集合
     */
    public List<BizAssemblyLog> selectBizAssemblyLogList(BizAssemblyLog bizAssemblyLog);

    /**
     * 新增装配流水日志
     * * @param bizAssemblyLog 装配流水日志
     * @return 结果
     */
    public int insertBizAssemblyLog(BizAssemblyLog bizAssemblyLog);

    /**
     * 修改装配流水日志
     * * @param bizAssemblyLog 装配流水日志
     * @return 结果
     */
    public int updateBizAssemblyLog(BizAssemblyLog bizAssemblyLog);

    /**
     * 批量删除装配流水日志
     * * @param logIds 需要删除的装配流水日志主键集合
     * @return 结果
     */
    public int deleteBizAssemblyLogByIds(Long[] logIds);

    /**
     * 删除装配流水日志信息
     * * @param logId 装配流水日志主键
     * @return 结果
     */
    public int deleteBizAssemblyLogById(Long logId);
}