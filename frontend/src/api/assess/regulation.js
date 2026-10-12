import request from '@/utils/request'

// 查询细则列表
export function listRegulation(query) {
  return request({
    url: '/assess/regulation/list',
    method: 'get',
    params: query
  })
}

// 查询细则详细
export function getRegulation(regulationId) {
  return request({
    url: '/assess/regulation/' + regulationId,
    method: 'get'
  })
}

// 新增细则
export function addRegulation(data) {
  return request({
    url: '/assess/regulation',
    method: 'post',
    data: data
  })
}

// 修改细则
export function updateRegulation(data) {
  return request({
    url: '/assess/regulation',
    method: 'put',
    data: data
  })
}

// 删除细则
export function delRegulation(regulationIds) {
  return request({
    url: '/assess/regulation/' + regulationIds,
    method: 'delete'
  })
}
