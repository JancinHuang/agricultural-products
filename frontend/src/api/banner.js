import request from '@/utils/request'
import { requirePathId } from '@/api/helpers'

export function getBannerList() {
  return request.get('/banner/list')
}

export function getBannerPage(params) {
  return request.get('/banner/page', { params })
}

export function addBanner(data) {
  return request.post('/banner', data)
}

export function updateBanner(data) {
  return request.put('/banner', data)
}

export function deleteBanner(id) {
  return request.delete(`/banner/${requirePathId(id, '轮播图ID')}`)
}
