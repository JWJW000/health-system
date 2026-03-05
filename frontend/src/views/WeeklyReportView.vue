<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../api/http'

const loading = ref(false)
const report = ref<any | null>(null)

const load = async () => {
  loading.value = true
  try {
    report.value = await http.get('/api/checkin/weekly-report')
  } finally {
    loading.value = false
  }
}

const formatDiff = (diff: number | null | undefined) => {
  if (diff == null) return '--'
  const v = diff.toFixed(1)
  return v + ' kg'
}

onMounted(load)
</script>

<template>
  <div class="layout">
    <div class="card">
      <div class="title">本周训练与打卡周报</div>
      <p class="subtitle">
        统计周期：
        <span v-if="report">
          {{ report.weekStart }} ~ {{ report.weekEnd }}
        </span>
      </p>
      <div v-if="loading" class="hint">加载中...</div>
      <div v-else-if="!report" class="hint">暂时无法获取周报，请稍后重试。</div>
      <div v-else class="grid">
        <div class="stat-card">
          <div class="stat-label">本周打卡天数</div>
          <div class="stat-value">
            {{ report.weekCheckins ?? 0 }}
            <span class="unit">天</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">最长连续打卡</div>
          <div class="stat-value">
            {{ report.longestStreak ?? 0 }}
            <span class="unit">天</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">本周体重变化</div>
          <div class="stat-value">
            {{ formatDiff(report.weightDiff) }}
          </div>
          <p class="small-hint">
            起始：{{ report.startWeight ?? '--' }} kg · 结束：{{ report.endWeight ?? '--' }} kg
          </p>
        </div>
        <div class="stat-card badge-card">
          <div class="stat-label">勋章</div>
          <div v-if="report.badge" class="badge">
            {{ report.badgeText }}
          </div>
          <div v-else class="small-hint">还没有解锁勋章，连续打卡 7 天即可获得。</div>
          <p v-if="report.nextBadgeHint" class="small-hint">
            {{ report.nextBadgeHint }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: radial-gradient(circle at top left, rgba(15, 23, 42, 0.95), rgba(15, 23, 42, 0.8));
  border-radius: 18px;
  padding: 18px 20px;
  border: 1px solid rgba(148, 163, 184, 0.35);
}

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.subtitle {
  margin: 4px 0 10px;
  font-size: 12px;
  color: #9ca3af;
}

.hint {
  font-size: 12px;
  color: #9ca3af;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 10px;
  margin-top: 8px;
}

.stat-card {
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(55, 65, 81, 0.9);
}

.stat-label {
  font-size: 12px;
  color: #9ca3af;
}

.stat-value {
  margin-top: 4px;
  font-size: 20px;
  font-weight: 600;
}

.unit {
  margin-left: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.small-hint {
  margin-top: 4px;
  font-size: 11px;
  color: #9ca3af;
}

.badge-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.badge {
  display: inline-block;
  margin-top: 4px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(34, 197, 94, 0.18);
  border: 1px solid rgba(34, 197, 94, 0.6);
  font-size: 12px;
  color: #bbf7d0;
}

@media (max-width: 960px) {
  .grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>

