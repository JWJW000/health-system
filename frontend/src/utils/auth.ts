export interface AuthUser {
  username: string
  role?: string
  avatarUrl?: string
}

const TOKEN_KEY = 'healthy_token'
const USER_KEY = 'healthy_user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    return null
  }
}

export function setUser(user: AuthUser) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearUser() {
  localStorage.removeItem(USER_KEY)
}

export function logout(router: { push: (path: string) => void }, redirectPath: string = '/login') {
  clearToken()
  clearUser()
  router.push(redirectPath)
}

