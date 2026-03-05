package com.ruoyi.process.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper; // 必须导入这个
import com.ruoyi.process.domain.BizWorker;

/**
 * 关键：必须加 @Mapper 注解
 */
@Mapper
public interface BizWorkerMapper
{
    /**
     * 注意：这里叫 selectBizWorkerById，不要写成 ByWorkerId
     */
    public BizWorker selectBizWorkerById(Long workerId);

    public List<BizWorker> selectBizWorkerList(BizWorker bizWorker);

    public int insertBizWorker(BizWorker bizWorker);

    public int updateBizWorker(BizWorker bizWorker);

    public int deleteBizWorkerById(Long workerId);

    public int deleteBizWorkerByIds(Long[] workerIds);
}