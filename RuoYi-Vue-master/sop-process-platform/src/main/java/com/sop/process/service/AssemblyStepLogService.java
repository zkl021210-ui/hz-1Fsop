package com.sop.process.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sop.process.dto.AssemblyStepLogDTO;
import com.sop.process.vo.AssemblyStepLogVO;

public interface AssemblyStepLogService {
    Page<AssemblyStepLogVO> pageAssemblyStepLogs(int page, int pageSize, Long taskId, String status);
    AssemblyStepLogVO getAssemblyStepLogById(Long id);
    AssemblyStepLogVO createAssemblyStepLog(AssemblyStepLogDTO dto);
    AssemblyStepLogVO updateAssemblyStepLog(Long id, AssemblyStepLogDTO dto);
    void deleteAssemblyStepLog(Long id);
}
