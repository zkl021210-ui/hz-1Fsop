package com.sop.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sop.common.exception.BusinessException;
import com.sop.common.exception.ErrorCode;
import com.sop.process.domain.Worker;
import com.sop.process.dto.WorkerDTO;
import com.sop.process.mapper.WorkerMapper;
import com.sop.process.service.WorkerService;
import com.sop.process.vo.WorkerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class WorkerServiceImpl extends ServiceImpl<WorkerMapper, Worker> implements WorkerService {

    @Override
    public Page<WorkerVO> pageWorkers(int page, int pageSize, String workerName, String status) {
        Page<Worker> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Worker> wrapper = new LambdaQueryWrapper<Worker>()
                .eq(Worker::getDeleted, 0)
                .like(workerName != null && !workerName.isEmpty(), Worker::getWorkerName, workerName)
                .eq(status != null && !status.isEmpty(), Worker::getStatus, status)
                .orderByDesc(Worker::getCreateTime);
        Page<Worker> result = this.page(pageParam, wrapper);
        Page<WorkerVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public WorkerVO getWorkerById(Long workerId) {
        Worker entity = this.getById(workerId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工人不存在");
        }
        return toVO(entity);
    }

    @Override
    public WorkerVO createWorker(WorkerDTO dto) {
        Worker entity = new Worker();
        BeanUtils.copyProperties(dto, entity);
        this.save(entity);
        return toVO(entity);
    }

    @Override
    public WorkerVO updateWorker(Long workerId, WorkerDTO dto) {
        Worker entity = this.getById(workerId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工人不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setWorkerId(workerId);
        this.updateById(entity);
        return toVO(entity);
    }

    @Override
    public void deleteWorker(Long workerId) {
        Worker entity = this.getById(workerId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工人不存在");
        }
        entity.setDeleted(1);
        this.updateById(entity);
    }

    private WorkerVO toVO(Worker entity) {
        WorkerVO vo = new WorkerVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
