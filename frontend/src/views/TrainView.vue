<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import http from '../api/http'

interface TrainAction {
  id: number
  name: string
  muscleGroup?: string
  description?: string
  setsSuggest?: number
  repsSuggest?: number
  weightSuggest?: string
  imageUrl?: string
  videoUrl?: string
  difficulty?: number
  equipment?: string
  durationMinutes?: number
}

const MUSCLE_GROUPS = [
  { key: 'chest', label: '练胸' },
  { key: 'back', label: '练背' },
  { key: 'leg', label: '练腿' },
  { key: 'shoulder', label: '练肩' },
  { key: 'core', label: '练核心' },
]

const currentGroup = ref('chest')
const actions = ref<TrainAction[]>([])
const trainPlan = ref<{ trainDays?: string[]; days?: number; patternLabel?: string }>({})
const aerobic = ref<any>({})
const weekPlan = ref<Record<string, string>>({})
const weekStart = ref(new Date())
const selectedDate = ref<string | null>(null)
const dayGroupSelected = ref<string | null>(null)
const dayActions = ref<TrainAction[]>([])
const actionDetail = ref<TrainAction | null>(null)

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

const formatDate = (d: Date) => {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const loadWeekPlan = async () => {
  const start = formatDate(weekStart.value)
  const resp = await http.get('/api/train/week-plan', {
    params: { start, days: 7 },
  })
  weekPlan.value = resp.plan || {}
  const todayStr = formatDate(new Date())
  if (weekPlan.value[todayStr]) {
    await selectDay(todayStr)
  } else {
    const firstKey = Object.keys(weekPlan.value)[0]
    if (firstKey) {
      await selectDay(firstKey)
    }
  }
}

const changeWeek = async (delta: number) => {
  const d = new Date(weekStart.value)
  d.setDate(d.getDate() + delta * 7)
  weekStart.value = d
  await loadWeekPlan()
}

const selectDay = async (dateStr: string) => {
  selectedDate.value = dateStr
  dayGroupSelected.value = null
  await loadDayDetail()
}

const loadDayDetail = async () => {
  if (!selectedDate.value) {
    dayActions.value = []
    return
  }
  const params: { date: string; group?: string } = { date: selectedDate.value }
  const planType = weekPlan.value[selectedDate.value]
  const effectiveGroup = dayGroupSelected.value || (planType !== 'REST' ? planType : null)
  if (effectiveGroup) params.group = effectiveGroup
  dayActions.value = await http.get('/api/train/day-detail', { params })
}

const onDayGroupChange = async (group: string) => {
  dayGroupSelected.value = group || null
  await loadDayDetail()
}

const loadAerobic = async () => {
  aerobic.value = await http.get('/api/train/aerobic', {})
}

const changeGroup = async (group: string) => {
  currentGroup.value = group
  await loadActions()
}

const openActionDetail = (a: TrainAction) => {
  actionDetail.value = a
}

const closeActionDetail = () => {
  actionDetail.value = null
}

const isTrainDay = () => {
  if (!selectedDate.value) return false
  const t = weekPlan.value[selectedDate.value]
  return t && t !== 'REST'
}

const sortedWeekPlan = computed(() =>
  Object.entries(weekPlan.value).sort(([a], [b]) => a.localeCompare(b))
)

onMounted(async () => {
  await Promise.all([loadActions(), loadPlan(), loadAerobic(), loadWeekPlan()])
})
</script>

<template>
  <div class="layout">
    <!-- 无氧训练：按肌群分类浏览动作，可点开看图片/视频/讲解 -->
    <div class="card">
      <h3 class="section-title">无氧训练 · 动作库</h3>
      <p class="section-desc">按训练侧重点选择肌群，点击动作可查看图片、视频与文字讲解（含注意事项）</p>
      <div class="tabs">
        <button
          v-for="g in MUSCLE_GROUPS"
          :key="g.key"
          class="tab"
          :class="{ active: currentGroup === g.key }"
          @click="changeGroup(g.key)"
        >
          {{ g.label }}
        </button>
      </div>
      <div class="actions-list">
        <div
          v-for="a in actions"
          :key="a.id"
          class="action-card clickable"
          @click="openActionDetail(a)"
        >
          <div class="action-preview" v-if="a.imageUrl">
            <img :src="a.imageUrl" :alt="a.name" class="action-thumb" />
          </div>
          <div class="action-body">
            <div class="action-header">
              <div class="action-name">{{ a.name }}</div>
              <div class="action-meta">
                <span v-if="a.setsSuggest">{{ a.setsSuggest }} 组</span>
                <span v-if="a.repsSuggest"> · 每组 {{ a.repsSuggest }} 次</span>
                <span v-if="a.durationMinutes"> · {{ a.durationMinutes }} 分钟</span>
              </div>
            </div>
            <p class="desc">{{ a.description }}</p>
            <p v-if="a.weightSuggest" class="meta">重量建议：{{ a.weightSuggest }}</p>
            <div class="action-actions">
              <button
                v-if="a.videoUrl"
                type="button"
                class="link-btn"
                @click.stop="openActionDetail(a)"
              >
                看视频
              </button>
              <span class="view-detail">点击查看讲解 →</span>
            </div>
          </div>
        </div>
        <div v-if="actions.length === 0" class="empty">暂无该肌群动作，请在后台补充动作库。</div>
      </div>
    </div>

    <div class="column">
      <!-- 训练/休息日安排：展示模式（练三休一/练四休一/练五休一） -->
      <div class="card">
        <div class="card-title">本周训练计划</div>
        <p v-if="trainPlan.patternLabel" class="pattern-label">
          当前模式：<strong>{{ trainPlan.patternLabel }}</strong>（根据您的增肌/减脂目标自动安排）
        </p>
        <div class="week-header">
          <button class="mini-btn" @click="changeWeek(-1)">&lt;</button>
          <span class="week-label">{{ formatDate(weekStart) }} 起的 7 天</span>
          <button class="mini-btn" @click="changeWeek(1)">&gt;</button>
        </div>
        <div class="week-grid">
          <button
            v-for="[date, type] in sortedWeekPlan"
            :key="date"
            class="week-cell"
            :class="{ active: selectedDate === date, rest: type === 'REST' }"
            @click="selectDay(date)"
          >
            <div class="week-date">{{ date }}</div>
            <div class="week-type">
              {{
                type === 'REST'
                  ? '休息'
                  : type === 'chest'
                    ? '胸'
                    : type === 'back'
                      ? '背'
                      : type === 'leg'
                        ? '腿'
                        : type === 'shoulder'
                          ? '肩'
                          : type === 'core'
                            ? '核心'
                            : type
              }}
            </div>
          </button>
        </div>
      </div>

      <!-- 训练日可自选今日训练部位 -->
      <div class="card">
        <div class="card-title">当日推荐动作</div>
        <div v-if="isTrainDay()" class="day-group-select">
          <label>自选今日训练部位：</label>
          <select :value="dayGroupSelected ?? ''" @change="onDayGroupChange(($event.target as HTMLSelectElement).value)">
            <option value="">
              {{ selectedDate && weekPlan[selectedDate] && weekPlan[selectedDate] !== 'REST' ? `按计划（今日推荐 ${MUSCLE_GROUPS.find(x => x.key === weekPlan[selectedDate])?.label || weekPlan[selectedDate]}）` : '按计划' }}
            </option>
            <option v-for="g in MUSCLE_GROUPS" :key="g.key" :value="g.key">{{ g.label }}</option>
          </select>
        </div>
        <div class="actions-list">
          <div
            v-for="a in dayActions"
            :key="a.id"
            class="action-card clickable"
            @click="openActionDetail(a)"
          >
            <div class="action-header">
              <div class="action-name">{{ a.name }}</div>
              <div class="action-meta">
                <span v-if="a.setsSuggest">{{ a.setsSuggest }} 组</span>
                <span v-if="a.repsSuggest"> · 每组 {{ a.repsSuggest }} 次</span>
              </div>
            </div>
            <p class="desc">{{ a.description }}</p>
            <p v-if="a.weightSuggest" class="meta">重量建议：{{ a.weightSuggest }}</p>
            <span class="view-detail">点击查看讲解 →</span>
          </div>
          <div v-if="!dayActions.length" class="empty">当日为休息日或暂无推荐动作；训练日可上方自选部位。</div>
        </div>
      </div>

      <!-- 有氧训练建议：类型、时长、频率、心率、时间安排建议 -->
      <div class="card aerobic-card">
        <div class="card-title">有氧训练建议</div>
        <p class="meta">推荐类型：{{ (aerobic.types || []).join('、') }}</p>
        <p class="meta">单次时长：{{ aerobic.durationMinutes }}</p>
        <p class="meta">每周频次：{{ aerobic.frequency }}</p>
        <p class="meta">心率要求：{{ aerobic.heartRate }}</p>
        <p v-if="aerobic.scheduleTip" class="schedule-tip">{{ aerobic.scheduleTip }}</p>
      </div>
    </div>

    <!-- 动作详情弹窗：图片、视频、文字讲解与注意事项 -->
    <Teleport to="body">
      <div v-if="actionDetail" class="modal-overlay" @click.self="closeActionDetail">
        <div class="modal-content">
          <div class="modal-header">
            <h3>{{ actionDetail.name }}</h3>
            <button class="modal-close" @click="closeActionDetail">×</button>
          </div>
          <div class="modal-body">
            <div v-if="actionDetail.imageUrl" class="detail-media">
              <img :src="actionDetail.imageUrl" :alt="actionDetail.name" class="detail-img" />
            </div>
            <div v-if="actionDetail.videoUrl" class="detail-media">
              <p class="detail-label">动作视频</p>
              <video
                class="detail-video"
                :src="actionDetail.videoUrl"
                controls
                preload="metadata"
              />
            </div>
            <div class="detail-section">
              <p class="detail-label">动作说明与注意事项</p>
              <p class="detail-desc">{{ actionDetail.description || '暂无文字说明' }}</p>
            </div>
            <div class="detail-meta">
              <span v-if="actionDetail.setsSuggest">推荐组数：{{ actionDetail.setsSuggest }} 组</span>
              <span v-if="actionDetail.repsSuggest">每组 {{ actionDetail.repsSuggest }} 次</span>
              <span v-if="actionDetail.weightSuggest">重量建议：{{ actionDetail.weightSuggest }}</span>
              <span v-if="actionDetail.durationMinutes">建议时长：{{ actionDetail.durationMinutes }} 分钟</span>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(0, 1.2fr);
  gap: 16px;
}

.section-title {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
}

.section-desc {
  margin: 0 0 12px;
  font-size: 12px;
  color: #94a3b8;
}

.card {
  background: radial-gradient(circle at top left, rgba(15, 23, 42, 0.95), rgba(15, 23, 42, 0.8));
  border-radius: 18px;
  padding: 16px 18px;
  border: 1px solid rgba(148, 163, 184, 0.35);
}

.tabs {
  display: flex;
  flex-wrap: wrap;
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
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.action-card.clickable {
  cursor: pointer;
}

.action-card.clickable:hover {
  border-color: rgba(34, 197, 94, 0.5);
}

.action-preview {
  flex-shrink: 0;
  width: 80px;
  height: 60px;
  border-radius: 10px;
  overflow: hidden;
  background: rgba(30, 41, 59, 0.8);
}

.action-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.action-body {
  flex: 1;
  min-width: 0;
}

.action-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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
  line-height: 1.4;
}

.meta {
  font-size: 11px;
  color: #9ca3af;
}

.action-actions {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.link-btn {
  font-size: 12px;
  color: #60a5fa;
  text-decoration: none;
}

.link-btn:hover {
  text-decoration: underline;
}

.view-detail {
  font-size: 11px;
  color: #22c55e;
}

.column {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pattern-label {
  margin: 0 0 8px;
  font-size: 12px;
  color: #94a3b8;
}

.pattern-label strong {
  color: #22c55e;
}

.week-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.week-label {
  font-size: 12px;
  color: #e5e7eb;
}

.mini-btn {
  border-radius: 999px;
  border: 1px solid rgba(55, 65, 81, 0.9);
  background: transparent;
  color: #e5e7eb;
  padding: 2px 8px;
  font-size: 11px;
  cursor: pointer;
}

.week-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
}

.week-cell {
  border-radius: 14px;
  border: 1px solid rgba(55, 65, 81, 0.9);
  background: rgba(15, 23, 42, 0.9);
  padding: 6px 8px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  cursor: pointer;
  font-size: 11px;
  color: #e5e7eb;
}

.week-cell.active {
  border-color: #22c55e;
}

.week-cell.rest {
  opacity: 0.85;
}

.week-date {
  font-weight: 500;
}

.week-type {
  font-size: 11px;
  color: #9ca3af;
}

.day-group-select {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.day-group-select label {
  font-size: 12px;
  color: #94a3b8;
}

.day-group-select select {
  padding: 6px 10px;
  border-radius: 10px;
  border: 1px solid rgba(55, 65, 81, 0.9);
  background: rgba(15, 23, 42, 0.95);
  color: #e5e7eb;
  font-size: 12px;
  min-width: 140px;
}

.schedule-tip {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(34, 197, 94, 0.08);
  border: 1px solid rgba(34, 197, 94, 0.25);
  font-size: 12px;
  color: #a7f3d0;
  line-height: 1.45;
}

.empty {
  font-size: 12px;
  color: #9ca3af;
}

/* 动作详情弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.98), rgba(15, 23, 42, 0.95));
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  max-width: 480px;
  width: 100%;
  max-height: 90vh;
  overflow: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 18px;
  border-bottom: 1px solid rgba(55, 65, 81, 0.9);
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.modal-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid rgba(75, 85, 99, 0.9);
  background: transparent;
  color: #e5e7eb;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}

.modal-close:hover {
  background: rgba(55, 65, 81, 0.5);
}

.modal-body {
  padding: 16px 18px;
}

.detail-media {
  margin-bottom: 14px;
}

.detail-video {
  width: 100%;
  border-radius: 12px;
  display: block;
  outline: none;
  background: #000;
}

.detail-img {
  width: 100%;
  border-radius: 12px;
  display: block;
}

.detail-label {
  margin: 0 0 6px;
  font-size: 12px;
  color: #94a3b8;
}

.detail-desc {
  margin: 0 0 12px;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
  font-size: 12px;
  color: #9ca3af;
}

.video-link {
  color: #60a5fa;
  text-decoration: none;
}

.video-link:hover {
  text-decoration: underline;
}

@media (max-width: 960px) {
  .layout {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>

