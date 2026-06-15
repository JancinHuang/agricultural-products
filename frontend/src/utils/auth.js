const TOKEN_KEY = 'token'
const USER_KEY = 'user'

export const getToken = () => localStorage.getItem(TOKEN_KEY) || ''

export const setToken = (token) => {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export const getStoredUser = () => {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch (error) {
    return null
  }
}

export const setStoredUser = (user) => {
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  } else {
    localStorage.removeItem(USER_KEY)
  }
}

export const clearAuth = () => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export const isAdminUser = (user) => Number(user?.role) === 1

export const parseJwtPayload = (token) => {
  try {
    const base64 = token.split('.')[1]
    if (!base64) return null
    const normalized = base64.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(normalized)
        .split('')
        .map(char => `%${(`00${char.charCodeAt(0).toString(16)}`).slice(-2)}`)
        .join('')
    )
    return JSON.parse(json)
  } catch (error) {
    return null
  }
}

export const isTokenExpired = (token) => {
  const payload = parseJwtPayload(token)
  if (!payload?.exp) return true
  return payload.exp * 1000 <= Date.now()
}
