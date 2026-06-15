import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getUserInfo } from '@/api/auth'
import router from '@/router'
import { clearAuth, getStoredUser, getToken, setStoredUser, setToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref(getStoredUser())

  const loginAction = async (loginForm) => {
    const res = await login(loginForm)
    token.value = res.data.token
    userInfo.value = res.data.user
    setToken(res.data.token)
    setStoredUser(res.data.user)
    return res
  }

  const getUserInfoAction = async () => {
    const res = await getUserInfo()
    userInfo.value = res.data
    setStoredUser(res.data)
    return res
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    clearAuth()
    router.push('/login')
  }

  return {
    token,
    userInfo,
    loginAction,
    getUserInfoAction,
    logout
  }
})
