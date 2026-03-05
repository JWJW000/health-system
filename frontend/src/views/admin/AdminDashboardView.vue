<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../../api/http'

const loading = ref(false)
const userCount = ref<number | null>(null)
const dietTemplateCount = ref<number | null>(null)
const trainActionCount = ref<number | null>(null)

const load = async () => {
  loading.value = true
  try {
    const [users, diets, actions] = await Promise.all([
      http.get('/api/admin/user/list', { params: {} }),
      http.get('/api/admin/diet/templates'),
      http.get('/api/admin/train/actions'),
    ])
    userCount.value = Array.isArray(users) ? users.length : null
    dietTemplateCount.value = Array.isArray(diets) ? diets.length : null
    trainActionCount.value = Array.isArray(actions) ? actions.length : null
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="dashboard">
    <div class="card">
      <div class="card-header">
        <div>
          <h2 class="title">仪表盘</h2>
        </div>
      </div>
      <div v-if="loading" class="hint">加载中...</div>
      <div v-else class="grid">
        <div class="stat-card">
          <div class="stat-label">注册用户数</div>
          <div class="stat-value">
            {{ userCount ?? '--' }}
            <span class="unit">人</span>
          </div>
          <p class="stat-desc">来自用户管理中的账号，包含普通用户与管理员。</p>
        </div>
        <div class="stat-card">
          <div class="stat-label">食谱模板数</div>
          <div class="stat-value">
            {{ dietTemplateCount ?? '--' }}
            <span class="unit">个</span>
          </div>
          <p class="stat-desc">用于为用户生成饮食推荐的基础模板数量。</p>
        </div>
        <div class="stat-card">
          <div class="stat-label">训练动作数</div>
          <div class="stat-value">
            {{ trainActionCount ?? '--' }}
            <span class="unit">个</span>
          </div>
          <p class="stat-desc">胸背腿肩核心等肌群的标准动作库条目。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: radial-gradient(circle at top left, rgba(15, 23, 42, 0.96), rgba(15, 23, 42, 0.88));
  border-radius: 18px;
  padding: 18px 20px;
  border: 1px solid rgba(148, 163, 184, 0.35);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
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

.hint {
  font-size: 12px;
  color: #9ca3af;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-top: 8px;
}

.stat-card {
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.98);
  border: 1px solid rgba(55, 65, 81, 0.9);
}

.stat-label {
  font-size: 12px;
  color: #9ca3af;
}

.stat-value {
  margin-top: 4px;
  font-size: 22px;
  font-weight: 600;
}

.unit {
  margin-left: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.stat-desc {
  margin-top: 4px;
  font-size: 11px;
  color: #9ca3af;
}

@media (max-width: 600px) {
  .card {
    padding: 14px 14px;
  }
}
</style>

