import request from '@/utils/request'

// ==========================================
// Part 1: SOP 基础管理接口 (增删改查)
// 对应后台：BizSopStepController
// ==========================================

// 查询SOP步骤列表
export function listSop(query) {
  return request({
    url: '/process/sop/list',
    method: 'get',
    params: query
  })
}

// 查询SOP步骤详细
export function getSop(stepId) {
  return request({
    url: '/process/sop/' + stepId,
    method: 'get'
  })
}

// 新增SOP步骤
export function addSop(data) {
  return request({
    url: '/process/sop',
    method: 'post',
    data: data
  })
}

// 修改SOP步骤
export function updateSop(data) {
  return request({
    url: '/process/sop',
    method: 'put',
    data: data
  })
}

// 删除SOP步骤
export function delSop(stepId) {
  return request({
    url: '/process/sop/' + stepId,
    method: 'delete'
  })
}

// ==========================================
// Part 2: SOP 执行控制接口 (作业台专用)
// 对应后台：SopExecutionController
// ==========================================

// 开始执行某个步骤 (通知Java: 写库 + 启动Python录像)
export function startSopStep(data) {
  return request({
    // ✅ 修正：后端 Controller 路径是 /process/sop，不是 /api/sop
    url: '/process/sop/step/start',
    method: 'post',
    data: data
  })
}

// 停止当前步骤 (通知Java: 完工更新 + 停止Python录像)
export function stopSopStep(data) {
  return request({
    // ✅ 修正：同上
    url: '/process/sop/step/stop',
    method: 'post',
    data: data
  })
}
