<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../api/http'
const loading = ref(false)
const history = ref<any[]>([])
const calendarDays = ref<string[]>([])
const year = ref(new Date().getFullYear())
const month = ref(new Date().getMonth() + 1)

const load = async () => {
  loading.value = true
  try {
    history.value = await http.get('/api/checkin/history')
    calendarDays.value = await http.get('/api/checkin/calendar', {
      params: { year: year.value, month: month.value },
    })
  } finally {
    loading.value = false
  }
}

const doCheckIn = async () => {
  try {
    await http.post('/api/checkin')
    await load()
  } catch (e: any) {
    alert(e?.message || '打卡失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="layout">
    <div class="card">
      <div class="header">
        <div>
          <div class="title">今日打卡</div>
          <p class="hint">完成训练后，一键打卡，系统自动累计你的坚持天数。</p>
        </div>
        <button class="btn" @click="doCheckIn">打卡</button>
      </div>
    </div>

    <div class="card">
      <div class="title">历史打卡记录</div>
      <div class="table">
        <div class="row head">
          <span>日期</span>
        </div>
        <div v-for="h in history" :key="h.id" class="row">
          <span>{{ h.checkDate }}</span>
        </div>
        <div v-if="history.length === 0" class="row empty">
          <span>暂无打卡记录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 1.3fr);
  gap: 16px;
}

.card {
  background: radial-gradient(circle at top left, rgba(15, 23, 42, 0.95), rgba(15, 23, 42, 0.8));
  border-radius: 18px;
  padding: 16px 18px;
  border: 1px solid rgba(148, 163, 184, 0.35);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 14px;
  font-weight: 600;
}

.hint {
  margin: 4px 0 0;
  font-size: 12px;
  color: #9ca3af;
}

.btn {
  border-radius: 999px;
  border: none;
  padding: 8px 18px;
  font-size: 13px;
  background: linear-gradient(90deg, #22c55e, #3b82f6);
  color: #f9fafb;
  cursor: pointer;
}

.table {
  margin-top: 10px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(55, 65, 81, 0.9);
}

.row {
  display: flex;
  padding: 6px 10px;
  font-size: 12px;
  border-bottom: 1px solid rgba(31, 41, 55, 0.9);
}

.row.head {
  background: rgba(15, 23, 42, 0.9);
  font-weight: 500;
}

.row.empty {
  color: #6b7280;
}

.row:last-child {
  border-bottom: none;
}

@media (max-width: 960px) {
  .layout {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>

