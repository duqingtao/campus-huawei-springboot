import request from '@/utils/request'

// 查询条款列表
export function listClause(query) {
  return request({
    url: '/assess/clause/list',
    method: 'get',
    params: query
  })
}

// 查询条款详细
export function getClause(clauseId) {
  return request({
    url: '/assess/clause/' + clauseId,
    method: 'get'
  })
}

// 根据细则ID查询条款树形结构
export function clauseTree(regulationId) {
  return request({
    url: '/assess/clause/tree/' + regulationId,
    method: 'get'
  })
}

// 新增条款
export function addClause(data) {
  return request({
    url: '/assess/clause',
    method: 'post',
    data: data
  })
}

// 修改条款
export function updateClause(data) {
  return request({
    url: '/assess/clause',
    method: 'put',
    data: data
  })
}

// 删除条款
export function delClause(clauseIds) {
  return request({
    url: '/assess/clause/' + clauseIds,
    method: 'delete'
  })
}

// 导入条款
export function importClause(data) {
  return request({
    url: '/assess/clause/import',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: data
  })
}
