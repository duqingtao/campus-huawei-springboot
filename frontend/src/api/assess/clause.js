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

// 预览导入条款（AI提取，不落库）
export function previewImportClause(data) {
  return request({
    url: '/assess/clause/import/preview',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 120000,
    data: data
  })
}

// 确认导入条款（批量落库）
export function confirmImportClause(regulationId, clauses) {
  return request({
    url: '/assess/clause/import/confirm',
    method: 'post',
    params: { regulationId: regulationId },
    data: clauses
  })
}
