<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElDropdown, ElDropdownMenu, ElDropdownItem, ElMessage } from 'element-plus'
import http from './api/http'
import { getUser, logout as doLogout } from './utils/auth'

const route = useRoute()
const router = useRouter()

const menus = [
  { path: '/', label: '概览' },
  { path: '/profile', label: '个人信息' },
  { path: '/diet', label: '饮食推荐' },
  { path: '/train', label: '训练计划' },
  { path: '/checkin', label: '打卡记录' },
  { path: '/weekly-report', label: '周报' },
]

const adminMenus = [
  { path: '/admin/dashboard', label: '仪表盘' },
  { path: '/admin/diets', label: '食谱模板' },
  { path: '/admin/train-actions', label: '训练动作库' },
  { path: '/admin/users', label: '用户管理' },
]

const avatarUrl = ref<string | null>(null)
const avatarInput = ref<HTMLInputElement | null>(null)
const avatarUploading = ref(false)

onMounted(async () => {
  if (route.path === '/login' || route.path === '/admin') return
  try {
    const profile = await http.get<{ avatarUrl?: string }>('/api/user/profile/me')
    if (profile?.avatarUrl) avatarUrl.value = profile.avatarUrl
  } catch {
    // 未登录或无 profile 时忽略
  }
})

const go = (path: string) => {
  if (path !== route.path) {
    router.push(path)
  }
}

const goAccount = () => go('/account')
const handleLogout = () => doLogout(router)
const handleAdminLogout = () => doLogout(router, '/admin')

const displayAvatarUrl = () => avatarUrl.value || getUser()?.avatarUrl || null

const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

const onAvatarFileChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file || !file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  avatarUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const url = await http.post<string>('/api/upload/avatar', formData)
    if (url) {
      avatarUrl.value = url
      ElMessage.success('头像更新成功')
    }
  } catch (err: any) {
    ElMessage.error(err?.message || '上传失败')
  } finally {
    avatarUploading.value = false
  }
}

const isAuthPage = computed(() => route.path === '/login' || route.path === '/admin')
const isAdminLayout = computed(() => route.path.startsWith('/admin') && route.path !== '/admin')
</script>

<template>
  <!-- 登录页 / 管理员登录页：作为独立全屏页面渲染，不包裹在应用外壳中 -->
  <router-view v-if="isAuthPage" />

  <!-- 管理后台路由：使用独立的后台布局外壳 -->
  <div v-else-if="isAdminLayout" class="admin-shell">
    <aside class="admin-sidebar">
      <div class="admin-logo">
        <span class="brand-main">Healthy Admin</span>
        <span class="brand-sub">管理后台</span>
      </div>
      <nav class="admin-nav">
        <button
          v-for="m in adminMenus"
          :key="m.path"
          class="admin-nav-item"
          :class="{ active: route.path === m.path }"
          @click="go(m.path)"
        >
          {{ m.label }}
        </button>
      </nav>
    </aside>
    <main class="admin-main">
      <header class="admin-topbar">
        <div class="admin-topbar-left">
          <h1 class="title">健康管理后台</h1>
        </div>
        <div class="admin-topbar-right">
          <button class="admin-logout-btn" type="button" @click="handleAdminLogout">
            退出登录
          </button>
        </div>
      </header>
      <section class="content">
        <router-view />
      </section>
    </main>
  </div>

  <!-- 其余路由：正常使用前台应用外壳布局 -->
  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="logo-area">
        <span class="brand-main">Healthy</span>
        <span class="brand-sub">健康计划</span>
      </div>
      <nav class="nav">
        <button
          v-for="m in menus"
          :key="m.path"
          class="nav-item"
          :class="{ active: route.path === m.path }"
          @click="go(m.path)"
        >
          {{ m.label }}
        </button>
      </nav>
    </aside>
    <main class="main">
      <header class="topbar">
        <div class="topbar-left">
          <h1 class="title">健身饮食与训练系统</h1>
          <p class="subtitle">个性化营养 · 科学训练 · 数据化反馈</p>
        </div>
        <div class="topbar-right">
          <el-dropdown trigger="click" @command="(cmd: string) => { if (cmd === 'account') goAccount(); else if (cmd === 'avatar') triggerAvatarUpload(); else if (cmd === 'logout') handleLogout(); }">
            <div class="avatar-wrap">
              <img
                v-if="displayAvatarUrl()"
                :src="displayAvatarUrl()!"
                alt="头像"
                class="avatar-img"
              />
              <span v-else class="avatar-placeholder">{{ (getUser()?.username || 'U').slice(0, 1).toUpperCase() }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="avatar" :disabled="avatarUploading">更换头像</el-dropdown-item>
                <el-dropdown-item command="account">账号设置</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            class="hidden-input"
            @change="onAvatarFileChange"
          />
        </div>
      </header>
      <section class="content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  min-height: 100vh;
  background: radial-gradient(circle at top left, #1f2937, #020617);
  color: #e5e7eb;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro Text', sans-serif;
}

.sidebar {
  width: 220px;
  padding: 24px 16px;
  backdrop-filter: blur(20px);
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.9), rgba(15, 23, 42, 0.6));
  border-right: 1px solid rgba(148, 163, 184, 0.15);
}

.logo-area {
  margin-bottom: 32px;
}

.brand-main {
  display: block;
  font-weight: 700;
  font-size: 20px;
  letter-spacing: 0.08em;
}

.brand-sub {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: #9ca3af;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  text-align: left;
  padding: 10px 12px;
  border-radius: 999px;
  border: none;
  background: transparent;
  color: #e5e7eb;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.18s ease-out;
}

.nav-item:hover {
  background: radial-gradient(circle at left, rgba(96, 165, 250, 0.2), transparent);
}

.nav-item.active {
  background: linear-gradient(90deg, #22c55e, #3b82f6);
  box-shadow: 0 0 24px rgba(56, 189, 248, 0.35);
}

.main {
  flex: 1;
  padding: 24px 32px;
  display: flex;
  flex-direction: column;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.25);
}

.topbar-left {
  flex: 1;
}

.topbar-right {
  flex-shrink: 0;
}

.title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.subtitle {
  margin: 4px 0 0;
  font-size: 12px;
  color: #9ca3af;
}

.avatar-wrap {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #22c55e, #3b82f6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.hidden-input {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}

.content {
  flex: 1;
  margin-top: 20px;
}

/* 管理后台整体布局 */
.admin-shell {
  display: flex;
  min-height: 100vh;
  background: radial-gradient(circle at top, #020617, #020617);
  color: #e5e7eb;
}

.admin-sidebar {
  width: 220px;
  padding: 20px 16px;
  backdrop-filter: blur(18px);
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.98), rgba(15, 23, 42, 0.9));
  border-right: 1px solid rgba(148, 163, 184, 0.25);
}

.admin-logo {
  margin-bottom: 28px;
}

.admin-nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.admin-nav-item {
  text-align: left;
  padding: 9px 12px;
  border-radius: 10px;
  border: none;
  background: transparent;
  color: #e5e7eb;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.16s ease-out;
}

.admin-nav-item:hover {
  background: rgba(30, 64, 175, 0.5);
}

.admin-nav-item.active {
  background: linear-gradient(135deg, #3b82f6, #22c55e);
  box-shadow: 0 0 18px rgba(37, 99, 235, 0.45);
}

.admin-main {
  flex: 1;
  padding: 20px 28px;
  display: flex;
  flex-direction: column;
}

.admin-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.35);
}

.admin-topbar-left {
  flex: 1;
}

.admin-topbar-right {
  flex-shrink: 0;
}

.admin-logout-btn {
  border-radius: 999px;
  border: 1px solid rgba(248, 113, 113, 0.8);
  background: rgba(248, 113, 113, 0.1);
  color: #fecaca;
  padding: 6px 14px;
  font-size: 12px;
  cursor: pointer;
}

@media (max-width: 960px) {
  .app-shell {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 14px;
  }
  .nav {
    flex-direction: row;
    overflow-x: auto;
    gap: 6px;
  }
  .logo-area {
    margin-bottom: 0;
  }
  .brand-main {
    font-size: 18px;
  }
  .brand-sub {
    display: none;
  }
  .nav-item {
    padding: 8px 10px;
    font-size: 12px;
    white-space: nowrap;
  }
  .main {
    padding: 16px;
  }
  .title {
    font-size: 18px;
  }
  .subtitle {
    font-size: 11px;
  }
  .content {
    margin-top: 14px;
  }

  .admin-shell {
    flex-direction: column;
  }
  .admin-sidebar {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
  }
  .admin-nav {
    flex-direction: row;
    overflow-x: auto;
    gap: 6px;
  }
  .admin-nav-item {
    white-space: nowrap;
    font-size: 12px;
    padding: 6px 10px;
  }
  .admin-main {
    padding: 16px;
  }
  .admin-topbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .admin-topbar-right {
    align-self: flex-end;
  }
}

@media (max-width: 600px) {
  .sidebar {
    padding: 10px 10px;
  }
  .brand-main {
    font-size: 16px;
  }
  .nav {
    gap: 4px;
  }
  .nav-item {
    padding: 6px 10px;
    font-size: 11px;
  }
  .main {
    padding: 12px;
  }
  .topbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .topbar-right {
    align-self: flex-end;
  }
  .title {
    font-size: 16px;
  }
  .subtitle {
    font-size: 10px;
  }

  .admin-sidebar {
    padding: 8px 10px;
  }
  .admin-main {
    padding: 14px;
  }
  .admin-logout-btn {
    padding: 6px 12px;
  }
}
</style>
