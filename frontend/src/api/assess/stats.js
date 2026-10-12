import request from '@/utils/request'

// 班级概览
export function statsOverview(query) {
  return request({
    url: '/assess/stats/overview',
    method: 'get',
    params: query
  })
}

// 本班学生申报明细列表
export function statsStudents(query) {
  return request({
    url: '/assess/stats/students',
    method: 'get',
    params: query
  })
}

// 导出班级申报汇总
export function exportStats(query) {
  return request({
    url: '/assess/stats/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}
