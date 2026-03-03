<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../api/http'

const loading = ref(false)
const summary = ref<{ total?: number; recent?: any[] }>({})
const bmr = ref<number | null>(null)

const fetchData = async () => {
  try {
    loading.value = true
    summary.value = await http.get('/api/checkin/summary')
    const profile = await http.get('/api/user/profile/me')
    if (profile) {
      const resp = await http.post('/api/nutrition/calc', {
        gender: profile.gender,
        age: profile.age,
        heightCm: profile.heightCm,
        weightKg: profile.weightKg,
        fitnessGoal: profile.fitnessGoal,
      })
      bmr.value = resp.bmr
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="grid">
    <div class="card highlight">
      <div class="card-title">今日概览</div>
      <div class="stats">
        <div class="stat">
          <div class="stat-label">累计打卡</div>
          <div class="stat-value">
            {{ summary.total ?? 0 }}
            <span class="stat-unit">天</span>
          </div>
        </div>
        <div class="stat">
          <div class="stat-label">基础代谢</div>
          <div class="stat-value">
            {{ bmr ?? '--' }}
            <span class="stat-unit">kcal</span>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-title">最近打卡</div>
      <ul class="list">
        <li v-for="item in summary.recent || []" :key="item.id" class="list-item">
          <span>{{ item.checkDate }}</span>
        </li>
        <li v-if="!summary.recent || summary.recent.length === 0" class="list-item empty">
          暂无打卡记录
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1.3fr);
  gap: 16px;
}

.card {
  background: radial-gradient(circle at top left, rgba(15, 23, 42, 0.95), rgba(15, 23, 42, 0.8));
  border-radius: 18px;
  padding: 18px 20px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.7);
}

.highlight {
  border-image: linear-gradient(120deg, #22c55e, #3b82f6, #a855f7) 1;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
}

.stats {
  display: flex;
  gap: 32px;
}

.stat-label {
  font-size: 12px;
  color: #9ca3af;
}

.stat-value {
  margin-top: 4px;
  font-size: 26px;
  font-weight: 600;
}

.stat-unit {
  margin-left: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.list-item {
  padding: 8px 0;
  border-bottom: 1px dashed rgba(55, 65, 81, 0.7);
  font-size: 13px;
}

.list-item:last-child {
  border-bottom: none;
}

.empty {
  color: #6b7280;
}

@media (max-width: 960px) {
  .grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>

