import request from '@/utils/request'
import { requirePathId } from '@/api/helpers'

export function getCategoryList() {
  return request.get('/category/list')
}

export function getCategoryPage(params) {
  return request.get('/category/page', { params })
}

export function getCategoryById(id) {
  return request.get(`/category/${requirePathId(id, '分类ID')}`)
}

export function addCategory(data) {
  return request.post('/category', data)
}

export function updateCategory(data) {
  return request.put('/category', data)
}

export function deleteCategory(id) {
  return request.delete(`/category/${requirePathId(id, '分类ID')}`)
}

export function getCategoryCount() {
  return request.get('/category/count')
}
