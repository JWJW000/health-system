import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './router'
import { getToken, getUser } from './utils/auth'

const app = createApp(App)

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  const user = getUser()

  // 需要登录的页面
  if (to.meta.requiresAuth && !token) {
    // 管理员页面未登录时，跳转到 /admin 登录
    if (to.meta.requiresAdmin) {
      return next({ path: '/admin' })
    }
    return next({ path: '/login' })
  }

  // 管理员权限校验
  if (to.meta.requiresAdmin) {
    if (!user || user.role !== 'ADMIN') {
      return next({ path: '/admin' })
    }
  }

  // 已登录管理员访问 /admin 时，直接进后台首页
  if (to.path === '/admin' && token && user?.role === 'ADMIN') {
    return next({ path: '/admin/diets' })
  }

  // 已登录用户访问普通登录页时，重定向到首页
  if (to.path === '/login' && token) {
    return next({ path: '/' })
  }

  next()
})

app.use(router)
app.mount('#app')
