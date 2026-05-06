package com.sop.process.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sop.process.dto.WorkerDTO;
import com.sop.process.vo.WorkerVO;

public interface WorkerService {
    Page<WorkerVO> pageWorkers(int page, int pageSize, String workerName, String status);
    WorkerVO getWorkerById(Long workerId);
    WorkerVO createWorker(WorkerDTO dto);
    WorkerVO updateWorker(Long workerId, WorkerDTO dto);
    void deleteWorker(Long workerId);
}
