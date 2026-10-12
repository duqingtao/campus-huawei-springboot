import request from '@/utils/request'

// 查询待审看板列表
export function listReview(query) {
  return request({
    url: '/assess/review/list',
    method: 'get',
    params: query
  })
}

// 审核操作
export function auditDeclaration(data) {
  return request({
    url: '/assess/review/audit',
    method: 'post',
    data: data
  })
}

// 查询审核历史
export function reviewHistory(declarationId) {
  return request({
    url: '/assess/review/history/' + declarationId,
    method: 'get'
  })
}

// 我的审核统计
export function reviewStats() {
  return request({
    url: '/assess/review/stats',
    method: 'get'
  })
}

// AI驳回意见草稿
export function draftComment(data) {
  return request({
    url: '/assess/review/draftComment',
    method: 'post',
    data: data
  })
}
