<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../../api/http'

interface SysUser {
  id: number
  username: string
  role: string
  createdTime?: string
}

const loading = ref(false)
const list = ref<SysUser[]>([])
const keyword = ref('')
const savingId = ref<number | null>(null)

const load = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (keyword.value) {
      params.keyword = keyword.value
    }
    list.value = await http.get('/api/admin/user/list', { params })
  } finally {
    loading.value = false
  }
}

const updateRole = async (user: SysUser) => {
  savingId.value = user.id
  try {
    await http.post('/api/admin/user/role', {
      userId: user.id,
      role: user.role,
    })
  } catch (e) {
    // 失败时重新刷新列表以回滚本地修改
    await load()
  } finally {
    savingId.value = null
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-layout">
    <div class="header">
      <div>
        <h2 class="title">用户管理</h2>
      </div>
    </div>

    <div class="card">
      <div class="toolbar">
        <input
          v-model="keyword"
          class="search"
          placeholder="按用户名搜索"
          @keyup.enter="load"
        />
        <button class="primary" @click="load">搜索</button>
      </div>
      <div class="table">
        <div class="row head">
          <span class="col-id">ID</span>
          <span class="col-name">用户名</span>
          <span class="col-role">角色</span>
          <span class="col-time">创建时间</span>
        </div>
        <div v-if="loading" class="row">
          <span>加载中...</span>
        </div>
        <div v-else-if="list.length === 0" class="row empty">
          <span>暂无用户记录</span>
        </div>
        <div v-else v-for="u in list" :key="u.id" class="row">
          <span class="col-id">#{{ u.id }}</span>
          <span class="col-name">{{ u.username }}</span>
          <span class="col-role">
            <select
              v-model="u.role"
              class="role-select"
              :disabled="savingId === u.id"
              @change="updateRole(u)"
            >
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </span>
          <span class="col-time">{{ u.createdTime || '-' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.subtitle {
  margin: 4px 0 0;
  font-size: 12px;
  color: #9ca3af;
}

.card {
  background: radial-gradient(circle at top left, rgba(15, 23, 42, 0.96), rgba(15, 23, 42, 0.88));
  border-radius: 18px;
  padding: 16px 18px;
  border: 1px solid rgba(148, 163, 184, 0.35);
}

.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.search {
  flex: 1;
  border-radius: 999px;
  border: 1px solid rgba(55, 65, 81, 0.9);
  background: rgba(15, 23, 42, 0.9);
  color: #e5e7eb;
  padding: 6px 10px;
  font-size: 12px;
}

.search:focus {
  outline: none;
  border-color: #22c55e;
}

button {
  border-radius: 999px;
  border: none;
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
}

.primary {
  background: linear-gradient(90deg, #22c55e, #3b82f6);
  color: #f9fafb;
}

.table {
  margin-top: 6px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(55, 65, 81, 0.9);
}

.row {
  display: grid;
  grid-template-columns: 0.8fr 2fr 1.2fr 2fr;
  gap: 8px;
  padding: 8px 10px;
  font-size: 12px;
  border-bottom: 1px solid rgba(31, 41, 55, 0.9);
}

.row.head {
  background: rgba(15, 23, 42, 0.95);
  font-weight: 500;
}

.row.empty {
  color: #6b7280;
}

.row:last-child {
  border-bottom: none;
}

.role-select {
  border-radius: 999px;
  border: 1px solid rgba(55, 65, 81, 0.9);
  background: rgba(15, 23, 42, 0.9);
  color: #e5e7eb;
  padding: 4px 8px;
  font-size: 12px;
}

.role-select:disabled {
  opacity: 0.6;
}

@media (max-width: 768px) {
  .row {
    grid-template-columns: 0.7fr 1.6fr 1fr 1.7fr;
  }
}

@media (max-width: 600px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  .primary {
    width: 100%;
    text-align: center;
  }
  .row.head {
    display: none;
  }
  .row {
    grid-template-columns: minmax(0, 1fr);
    gap: 4px;
  }
  .col-id,
  .col-name,
  .col-role,
  .col-time {
    display: block;
  }
}
</style>

