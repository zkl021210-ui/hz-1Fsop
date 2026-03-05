import request from '@/utils/request'

// 1. 开工登记 (后端会自动开启第1步录像)
export function startTask(data) {
  return request({
    url: '/process/task/start',
    method: 'post',
    data: data
  })
}

// 2. 人工强制下一步 (作为 AI 的兜底方案)
export function nextStepManual(data) {
  return request({
    url: '/process/task/next',
    method: 'post',
    data: data
  })
}

// 3. 检查设备状态 (用于前端轮询同步)
export function checkDeviceStatus(deviceSn) {
  return request({
    url: '/process/task/check/' + deviceSn,
    method: 'get'
  })
}

// 4. 暂停任务
export function pauseTask(data) {
  return request({
    url: '/process/task/pause',
    method: 'post',
    data: data
  })
}

// 5. 完工 (如果需要单独调用)
export function finishTask(data) {
  return request({
    url: '/process/task/finish',
    method: 'post',
    data: data
  })
}

// 保留您原有的列表接口
export function listTask(query) {
  return request({
    url: '/process/task/list',
    method: 'get',
    params: query
  })
}

// 保留 updateTask 以防其他组件引用，但在 Workbench 中我们主要用 nextStepManual
export function updateTask(data) {
  return request({
    url: '/process/task',
    method: 'put',
    data: data
  })
}
