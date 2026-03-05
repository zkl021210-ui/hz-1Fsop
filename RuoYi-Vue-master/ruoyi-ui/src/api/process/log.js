import request from '@/utils/request'

// ⚠️ 重点：这里必须和后端 Controller 的 @RequestMapping("/process/assemblyLog") 保持一致
const BASE_URL = '/process/assemblyLog'

// 查询装配作业流水列表
export function listLog(query) {
  return request({
    url: BASE_URL + '/list',
    method: 'get',
    params: query
  })
}

// 查询装配作业流水详细
export function getLog(logId) {
  return request({
    url: BASE_URL + '/' + logId,
    method: 'get'
  })
}

// 新增装配作业流水
export function addLog(data) {
  return request({
    url: BASE_URL,
    method: 'post',
    data: data
  })
}

// 修改装配作业流水
export function updateLog(data) {
  return request({
    url: BASE_URL,
    method: 'put',
    data: data
  })
}

// 删除装配作业流水
export function delLog(logId) {
  return request({
    url: BASE_URL + '/' + logId,
    method: 'delete'
  })
}
