package com.ruoyi.process.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.process.mapper.BizAssemblyLogMapper;
import com.ruoyi.process.domain.BizAssemblyLog;
import com.ruoyi.process.service.IBizAssemblyLogService;

@Service
public class BizAssemblyLogServiceImpl implements IBizAssemblyLogService
{
    @Autowired
    private BizAssemblyLogMapper bizAssemblyLogMapper;

    @Override
    public BizAssemblyLog selectBizAssemblyLogById(Long logId)
    {
        return bizAssemblyLogMapper.selectBizAssemblyLogById(logId);
    }

    @Override
    public List<BizAssemblyLog> selectBizAssemblyLogList(BizAssemblyLog bizAssemblyLog)
    {
        return bizAssemblyLogMapper.selectBizAssemblyLogList(bizAssemblyLog);
    }

    @Override
    public int insertBizAssemblyLog(BizAssemblyLog bizAssemblyLog)
    {
        if (bizAssemblyLog.getCreateTime() == null) {
            bizAssemblyLog.setCreateTime(DateUtils.getNowDate());
        }
        return bizAssemblyLogMapper.insertBizAssemblyLog(bizAssemblyLog);
    }

    @Override
    public int updateBizAssemblyLog(BizAssemblyLog bizAssemblyLog)
    {
        return bizAssemblyLogMapper.updateBizAssemblyLog(bizAssemblyLog);
    }

    @Override
    public int deleteBizAssemblyLogByIds(Long[] logIds)
    {
        return bizAssemblyLogMapper.deleteBizAssemblyLogByIds(logIds);
    }

    @Override
    public int deleteBizAssemblyLogById(Long logId)
    {
        return bizAssemblyLogMapper.deleteBizAssemblyLogById(logId);
    }
}