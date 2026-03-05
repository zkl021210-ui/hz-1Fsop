package com.ruoyi.process.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.process.mapper.BizWorkerMapper;
import com.ruoyi.process.domain.BizWorker;
import com.ruoyi.process.service.IBizWorkerService;

@Service
public class BizWorkerServiceImpl implements IBizWorkerService
{
    @Autowired
    private BizWorkerMapper bizWorkerMapper;

    /**
     * 修复点：方法名必须和 Mapper 里的一样，也和 Interface 里的一样
     * 统一叫：selectBizWorkerById
     */
    @Override
    public BizWorker selectBizWorkerById(Long workerId)
    {
        return bizWorkerMapper.selectBizWorkerById(workerId);
    }

    @Override
    public List<BizWorker> selectBizWorkerList(BizWorker bizWorker)
    {
        return bizWorkerMapper.selectBizWorkerList(bizWorker);
    }

    @Override
    public int insertBizWorker(BizWorker bizWorker)
    {
        bizWorker.setCreateTime(DateUtils.getNowDate());
        return bizWorkerMapper.insertBizWorker(bizWorker);
    }

    @Override
    public int updateBizWorker(BizWorker bizWorker)
    {
        bizWorker.setUpdateTime(DateUtils.getNowDate());
        return bizWorkerMapper.updateBizWorker(bizWorker);
    }

    @Override
    public int deleteBizWorkerByIds(Long[] workerIds)
    {
        return bizWorkerMapper.deleteBizWorkerByIds(workerIds);
    }

    @Override
    public int deleteBizWorkerById(Long workerId)
    {
        return bizWorkerMapper.deleteBizWorkerById(workerId);
    }
}