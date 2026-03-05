package com.ruoyi.process.service;

import java.util.List;
import com.ruoyi.process.domain.BizWorker;

public interface IBizWorkerService
{
    // 统一标准：ById
    public BizWorker selectBizWorkerById(Long workerId);

    public List<BizWorker> selectBizWorkerList(BizWorker bizWorker);

    public int insertBizWorker(BizWorker bizWorker);

    public int updateBizWorker(BizWorker bizWorker);

    public int deleteBizWorkerByIds(Long[] workerIds);

    public int deleteBizWorkerById(Long workerId);
}