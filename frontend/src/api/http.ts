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

instance.interceptors.response.use(
  (resp) => {
    const data = resp.data
    if (data && typeof data.code !== 'undefined' && data.code !== 0) {
      if (data.code === 401) {
        clearToken()
        clearUser()
        window.location.href = '/login'
      }
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data.data ?? data
  },
  (error) => {
    if (error?.response?.status === 401) {
      clearToken()
      clearUser()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default instance

