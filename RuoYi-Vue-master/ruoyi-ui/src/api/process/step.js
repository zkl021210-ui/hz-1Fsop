import request from '@/utils/request'

// 查询SOP步骤配置列表
export function listStep(query) {
  return request({
    url: '/process/step/list',
    method: 'get',
    params: query
  })
}

// 查询SOP步骤配置详细
export function getStep(stepId) {
  return request({
    url: '/process/step/' + stepId,
    method: 'get'
  })
}

// 新增SOP步骤配置
export function addStep(data) {
  return request({
    url: '/process/step',
    method: 'post',
    data: data
  })
}

// 修改SOP步骤配置
export function updateStep(data) {
  return request({
    url: '/process/step',
    method: 'put',
    data: data
  })
}

// 删除SOP步骤配置
export function delStep(stepId) {
  return request({
    url: '/process/step/' + stepId,
    method: 'delete'
  })
}
