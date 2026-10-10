import request from '@/utils/request'

// 查询综测申报单列表
export function listDeclaration(query) {
  return request({
    url: '/assess/declaration/list',
    method: 'get',
    params: query
  })
}

// 查询综测申报单详细
export function getDeclaration(declarationId) {
  return request({
    url: '/assess/declaration/' + declarationId,
    method: 'get'
  })
}

// 新增综测申报单
export function addDeclaration(data) {
  return request({
    url: '/assess/declaration',
    method: 'post',
    data: data
  })
}

// 修改综测申报单
export function updateDeclaration(data) {
  return request({
    url: '/assess/declaration',
    method: 'put',
    data: data
  })
}

// 删除综测申报单
export function delDeclaration(declarationIds) {
  return request({
    url: '/assess/declaration/' + declarationIds,
    method: 'delete'
  })
}

// 提交综测申报单
export function submitDeclaration(declarationId) {
  return request({
    url: '/assess/declaration/submit/' + declarationId,
    method: 'post'
  })
}

// ==================== 规则引擎配置 ====================

// 查询规则配置列表
export function listRule(query) {
  return request({
    url: '/assess/rule/list',
    method: 'get',
    params: query
  })
}

// 查询规则配置详细
export function getRule(ruleId) {
  return request({
    url: '/assess/rule/' + ruleId,
    method: 'get'
  })
}

// 新增规则配置
export function addRule(data) {
  return request({
    url: '/assess/rule',
    method: 'post',
    data: data
  })
}

// 修改规则配置
export function updateRule(data) {
  return request({
    url: '/assess/rule',
    method: 'put',
    data: data
  })
}

// 删除规则配置
export function delRule(ruleIds) {
  return request({
    url: '/assess/rule/' + ruleIds,
    method: 'delete'
  })
}
