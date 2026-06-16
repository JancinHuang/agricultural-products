import request from '@/utils/request'
import { requirePathId } from '@/api/helpers'

export function getProductPage(params) {
  return request.get('/product/page', { params })
}

export function getProductById(id) {
  return request.get(`/product/${requirePathId(id, '商品ID')}`)
}

export function getHotProducts(limit) {
  return request.get('/product/hot', { params: { limit } })
}

export function getProductsByCategory(categoryId) {
  return request.get(`/product/category/${requirePathId(categoryId, '分类ID')}`)
}

export function addProduct(data) {
  return request.post('/product', data)
}

export function updateProduct(data) {
  return request.put('/product', data)
}

export function deleteProduct(id) {
  return request.delete(`/product/${requirePathId(id, '商品ID')}`)
}

export function getProductCount() {
  return request.get('/product/count')
}

export function searchProducts(params) {
  return request.get('/product/search', { params })
}
