import request from '@/utils/request'
import { requirePathId, requirePathValue } from '@/api/helpers'

export function getUserList(params) {
  return request.get('/user/list', { params })
}

export function getUserById(id) {
  return request.get(`/user/${requirePathId(id, '用户ID')}`)
}

export function addUser(data) {
  return request.post('/user', data)
}

export function updateUser(data) {
  return request.put('/user', data)
}

export function deleteUser(id) {
  return request.delete(`/user/${requirePathId(id, '用户ID')}`)
}

export function updateUserStatus(id, status) {
  return request.put(`/user/status/${requirePathId(id, '用户ID')}/${requirePathValue(status, '用户状态')}`)
}

export function getUserCount() {
  return request.get('/user/count')
}

export function updateUserInfo(data) {
  return request.put('/user/info', data)
}

export function updatePassword(data) {
  return request.put('/user/password', data)
}
