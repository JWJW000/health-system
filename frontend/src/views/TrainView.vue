<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../api/http'
const currentGroup = ref('chest')
const actions = ref<any[]>([])
const trainPlan = ref<{ trainDays?: string[]; days?: number }>({})
const aerobic = ref<any>({})

const loadActions = async () => {
  actions.value = await http.get('/api/train/actions', {
    params: { group: currentGroup.value },
  })
}

const loadPlan = async () => {
  trainPlan.value = await http.get('/api/train/plan', {
    params: { days: 14 },
  })
}

const loadAerobic = async () => {
  aerobic.value = await http.get('/api/train/aerobic', {
  })
}

const changeGroup = async (group: string) => {
  currentGroup.value = group
  await loadActions()
}

onMounted(async () => {
  await Promise.all([loadActions(), loadPlan(), loadAerobic()])
})
</script>

<template>
  <div class="layout">
    <div class="card">
      <div class="tabs">
        <button
          v-for="g in [
            { key: 'chest', label: '练胸' },
            { key: 'back', label: '练背' },
            { key: 'leg', label: '练腿' },
            { key: 'shoulder', label: '练肩' },
            { key: 'core', label: '练核心' },
          ]"
          :key="g.key"
          class="tab"
          :class="{ active: currentGroup === g.key }"
          @click="changeGroup(g.key)"
        >
          {{ g.label }}
        </button>
      </div>
      <div class="actions-list">
        <div v-for="a in actions" :key="a.id" class="action-card">
          <div class="action-header">
            <div class="action-name">{{ a.name }}</div>
            <div class="action-meta">
              {{ a.setsSuggest ? a.setsSuggest + ' 组' : '' }}
              <span v-if="a.repsSuggest"> · 每组 {{ a.repsSuggest }} 次</span>
            </div>
          </div>
          <p class="desc">{{ a.description }}</p>
          <p v-if="a.weightSuggest" class="meta">重量建议：{{ a.weightSuggest }}</p>
        </div>
        <div v-if="actions.length === 0" class="empty">暂无该肌群动作，请在后台补充动作库。</div>
      </div>
    </div>

    <div class="column">
      <div class="card">
        <div class="card-title">未来 14 天训练/休息日</div>
        <div class="plan">
          <span
            v-for="d in trainPlan.trainDays || []"
            :key="d"
            class="day"
          >
            {{ d }}
          </span>
        </div>
      </div>

      <div class="card">
        <div class="card-title">有氧训练建议</div>
        <p class="meta">类型：{{ (aerobic.types || []).join(' / ') }}</p>
        <p class="meta">单次时长：{{ aerobic.durationMinutes }}</p>
        <p class="meta">每周频次：{{ aerobic.frequency }}</p>
        <p class="meta">心率建议：{{ aerobic.heartRate }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(0, 1.2fr);
  gap: 16px;
}

.card {
  background: radial-gradient(circle at top left, rgba(15, 23, 42, 0.95), rgba(15, 23, 42, 0.8));
  border-radius: 18px;
  padding: 16px 18px;
  border: 1px solid rgba(148, 163, 184, 0.35);
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.tab {
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  border: 1px solid rgba(55, 65, 81, 0.9);
  background: transparent;
  color: #e5e7eb;
  cursor: pointer;
}

.tab.active {
  background: linear-gradient(90deg, #22c55e, #3b82f6);
  border-color: transparent;
}

.actions-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-card {
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(55, 65, 81, 0.9);
}

.action-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.action-name {
  font-weight: 600;
}

.action-meta {
  font-size: 11px;
  color: #9ca3af;
}

.desc {
  margin: 6px 0 2px;
  font-size: 12px;
}

.meta {
  font-size: 11px;
  color: #9ca3af;
}

.column {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plan {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.day {
  padding: 4px 8px;
  border-radius: 999px;
  border: 1px solid rgba(55, 65, 81, 0.9);
  font-size: 11px;
}

.empty {
  font-size: 12px;
  color: #9ca3af;
}

@media (max-width: 960px) {
  .layout {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>

