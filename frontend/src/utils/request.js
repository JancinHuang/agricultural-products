import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { clearAuth, getToken } from '@/utils/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

const clearAuthAndRedirect = () => {
  clearAuth()
  if (router.currentRoute.value.path !== '/login') {
    router.push('/login')
  }
}

request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        clearAuthAndRedirect()
        ElMessage.error('登录已过期，请重新登录')
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      clearAuthAndRedirect()
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(error)
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
