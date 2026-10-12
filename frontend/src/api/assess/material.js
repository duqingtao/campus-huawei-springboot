import request from '@/utils/request'

// 查询材料列表
export function listMaterial(query) {
  return request({
    url: '/assess/material/list',
    method: 'get',
    params: query
  })
}

// 查询材料详细
export function getMaterial(materialId) {
  return request({
    url: '/assess/material/' + materialId,
    method: 'get'
  })
}

// 修改材料
export function updateMaterial(data) {
  return request({
    url: '/assess/material',
    method: 'put',
    data: data
  })
}

// 删除材料
export function delMaterial(materialIds) {
  return request({
    url: '/assess/material/' + materialIds,
    method: 'delete'
  })
}

// 重新识别（重置OCR+抽取状态）
export function reprocessMaterial(materialId) {
  return request({
    url: '/assess/material/reprocess/' + materialId,
    method: 'post'
  })
}

// 导出材料列表
export function exportMaterial(query) {
  return request({
    url: '/assess/material/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}
