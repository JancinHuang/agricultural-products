import request from '@/utils/request'
import { requirePathId, requirePathValue } from '@/api/helpers'

export function getOrderPage(params) {
  return request.get('/order/page', { params })
}

export function getOrderById(id) {
  return request.get(`/order/${requirePathId(id, '订单ID')}`)
}

export function addOrder(data) {
  return request.post('/order', data)
}

export function updateOrder(data) {
  return request.put('/order', data)
}

export function deleteOrder(id) {
  return request.delete(`/order/${requirePathId(id, '订单ID')}`)
}

export function updateOrderStatus(id, status) {
  return request.put(`/order/status/${requirePathId(id, '订单ID')}/${requirePathValue(status, '订单状态')}`)
}

export function getOrderCount() {
  return request.get('/order/count')
}

export function getUserOrders() {
  return request.get('/order/user')
}

export function getOrderDetail(id) {
  return request.get(`/order/detail/${requirePathId(id, '订单ID')}`)
}

export function createOrder(data) {
  return request.post('/order/create', data)
}

export function payOrder(id) {
  return request.put(`/order/pay/${requirePathId(id, '订单ID')}`)
}

export function cancelOrder(id) {
  return request.put(`/order/cancel/${requirePathId(id, '订单ID')}`)
}

export function confirmOrder(id) {
  return request.put(`/order/confirm/${requirePathId(id, '订单ID')}`)
}
