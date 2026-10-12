import request from '@/utils/request'

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
