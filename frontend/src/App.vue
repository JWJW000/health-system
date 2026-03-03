<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const menus = [
  { path: '/', label: '概览' },
  { path: '/profile', label: '个人信息' },
  { path: '/diet', label: '饮食推荐' },
  { path: '/train', label: '训练计划' },
  { path: '/checkin', label: '打卡记录' },
]

const go = (path: string) => {
  if (path !== route.path) {
    router.push(path)
  }
}
</script>

<template>
  <!-- 登录页 / 管理员登录页：作为独立全屏页面渲染，不包裹在应用外壳中 -->
  <router-view v-if="route.path === '/login' || route.path === '/admin'" />

  <!-- 其余路由：正常使用应用外壳布局 -->
  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="logo-area">
        <span class="brand-main">Healthy</span>
        <span class="brand-sub">Fitness Planner</span>
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
        <h1 class="title">健身饮食与训练系统</h1>
        <p class="subtitle">个性化营养 · 科学训练 · 数据化反馈</p>
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
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.25);
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

.content {
  flex: 1;
  margin-top: 20px;
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
}
</style>
