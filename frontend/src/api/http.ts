import axios from 'axios'
import { getToken, clearToken, clearUser } from '../utils/auth'

const instance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
})

instance.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers || {}
    ;(config.headers as any).Authorization = `Bearer ${token}`
  }
  return config
})

function redirectToLogin() {
  clearToken()
  clearUser()
  const path = window.location.pathname
  // 已在登录页时不再跳转，避免 401 导致反复刷新
  if (path === '/login' || path === '/admin') return
  if (path.startsWith('/admin')) {
    window.location.href = '/admin'
  } else {
    window.location.href = '/login'
  }
}

instance.interceptors.response.use(
  (resp) => {
    const data = resp.data
    if (data && typeof data.code !== 'undefined' && data.code !== 0) {
      if (data.code === 401) {
        redirectToLogin()
      }
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data.data ?? data
  },
  (error) => {
    if (error?.response?.status === 401) {
      redirectToLogin()
    }
    return Promise.reject(error)
  },
)

export default instance

