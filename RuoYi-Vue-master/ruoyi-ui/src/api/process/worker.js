import request from '@/utils/request'

// 查询工人信息列表
export function listWorker(query) {
  return request({
    url: '/process/worker/list',
    method: 'get',
    params: query
  })
}

// 查询工人信息详细
export function getWorker(workerId) {
  return request({
    url: '/process/worker/' + workerId,
    method: 'get'
  })
}

// 新增工人信息
export function addWorker(data) {
  return request({
    url: '/process/worker',
    method: 'post',
    data: data
  })
}

// 修改工人信息
export function updateWorker(data) {
  return request({
    url: '/process/worker',
    method: 'put',
    data: data
  })
}

// 删除工人信息
export function delWorker(workerId) {
  return request({
    url: '/process/worker/' + workerId,
    method: 'delete'
  })
}
