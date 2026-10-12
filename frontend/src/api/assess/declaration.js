import request from '@/utils/request'

// 查询申报单列表
export function listDeclaration(query) {
  return request({
    url: '/assess/declaration/list',
    method: 'get',
    params: query
  })
}

// 查询申报单详细（含明细列表）
export function getDeclaration(declarationId) {
  return request({
    url: '/assess/declaration/' + declarationId,
    method: 'get'
  })
}

// 新增申报单（草稿）
export function addDeclaration(data) {
  return request({
    url: '/assess/declaration',
    method: 'post',
    data: data
  })
}

// 修改申报单（仅草稿状态）
export function updateDeclaration(data) {
  return request({
    url: '/assess/declaration',
    method: 'put',
    data: data
  })
}

// 删除申报单（仅草稿状态）
export function delDeclaration(declarationIds) {
  return request({
    url: '/assess/declaration/' + declarationIds,
    method: 'delete'
  })
}

// 提交申报单（触发AI匹配+规则引擎+定分+汇总）
// 后端权限注解为 assess:declaration:edit，但 SQL 菜单 2207 定义为 assess:declaration:submit
// 前端 v-hasPermi 使用 ['assess:declaration:submit'] 与 SQL 保持一致
export function submitDeclaration(declarationId) {
  return request({
    url: '/assess/declaration/submit/' + declarationId,
    method: 'post'
  })
}

// 导出申报单列表
export function exportDeclaration(query) {
  return request({
    url: '/assess/declaration/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}
