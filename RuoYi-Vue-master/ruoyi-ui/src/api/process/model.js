import request from '@/utils/request'

// 查询转辙机型号列表
export function listModel(query) {
  return request({
    url: '/process/model/list',
    method: 'get',
    params: query
  })
}

// 查询转辙机型号详细
export function getModel(modelId) {
  return request({
    url: '/process/model/' + modelId,
    method: 'get'
  })
}

// 新增转辙机型号
export function addModel(data) {
  return request({
    url: '/process/model',
    method: 'post',
    data: data
  })
}

// 修改转辙机型号
export function updateModel(data) {
  return request({
    url: '/process/model',
    method: 'put',
    data: data
  })
}

// 删除转辙机型号
export function delModel(modelId) {
  return request({
    url: '/process/model/' + modelId,
    method: 'delete'
  })
}
