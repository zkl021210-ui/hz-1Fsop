package com.sop.process.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sop.process.dto.AssemblyTaskDTO;
import com.sop.process.vo.AssemblyTaskVO;

/**
 * 装配任务 Service 接口
 *
 * @author SOP Team
 */
public interface AssemblyTaskService {

    /**
     * 分页查询装配任务
     */
    Page<AssemblyTaskVO> pageAssemblyTasks(int page, int pageSize, String modelCode, String status);

    /**
     * 根据ID查询装配任务
     */
    AssemblyTaskVO getAssemblyTaskById(Long taskId);

    /**
     * 新增装配任务
     */
    AssemblyTaskVO createAssemblyTask(AssemblyTaskDTO dto);

    /**
     * 修改装配任务
     */
    AssemblyTaskVO updateAssemblyTask(Long taskId, AssemblyTaskDTO dto);

    /**
     * 删除装配任务
     */
    void deleteAssemblyTask(Long taskId);
}
