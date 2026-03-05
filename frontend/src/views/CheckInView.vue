<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { ElMessage, ElDialog, ElUpload } from 'element-plus'
import type { UploadFile, UploadInstance } from 'element-plus'
import * as echarts from 'echarts'
import http from '../api/http'

const loading = ref(false)
const history = ref<any[]>([])
const calendarDays = ref<string[]>([])
const year = ref(new Date().getFullYear())
const month = ref(new Date().getMonth() + 1)

const checkInDialogVisible = ref(false)
const checkInWeight = ref<string>('')
const checkInFileList = ref<UploadFile[]>([])
const checkInUploadRef = ref<UploadInstance | null>(null)

const calendarDom = ref<HTMLDivElement | null>(null)
let calendarChart: echarts.ECharts | null = null

const detailVisible = ref(false)
const selectedDateStr = ref<string>('')

const weekNames = ['日', '一', '二', '三', '四', '五', '六']
const formatDateLabel = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr + 'T00:00:00')
  const day = d.getDate()
  const week = d.getDay()
  return `${day}日 周${weekNames[week]}`
}

/** 生成当月所有日期的打卡数据，便于每个格子都显示几号+星期几 */
const getCalendarData = () => {
  const set = new Set(calendarDays.value || [])
  const y = year.value
  const m = month.value
  const lastDay = new Date(y, m, 0).getDate()
  const data: [string, number][] = []
  for (let d = 1; d <= lastDay; d++) {
    const dateStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    data.push([dateStr, set.has(dateStr) ? 1 : 0])
  }
  return data
}

const buildCalendar = () => {
  if (!calendarDom.value) return
  if (!calendarChart) {
    calendarChart = echarts.init(calendarDom.value)
  }
  const data = getCalendarData()
  calendarChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      position: 'top',
      formatter: (p: any) => {
        const d = new Date(p.value[0] + 'T00:00:00')
        const day = d.getDate()
        const week = d.getDay()
        return `已打卡：${p.value[0]}（${day}日 周${weekNames[week]}）`
      },
    },
    visualMap: {
      min: 0,
      max: 1,
      show: false,
      inRange: {
        color: ['transparent', '#22c55e'],
      },
    },
    calendar: {
      top: 30,
      left: 10,
      right: 10,
      cellSize: ['auto', 22],
      range: `${year.value}-${month.value.toString().padStart(2, '0')}`,
      splitLine: { show: false },
      dayLabel: {
        color: '#9ca3af',
        nameMap: 'cn',
        firstDay: 1,
        margin: 4,
      },
      monthLabel: { color: '#9ca3af' },
      yearLabel: { show: false },
      itemStyle: {
        borderColor: '#1f2937',
        borderWidth: 1,
        color: 'transparent',
      },
    },
    series: [
      {
        type: 'heatmap',
        coordinateSystem: 'calendar',
        data,
        label: {
          show: true,
          color: '#e5e7eb',
          fontSize: 10,
          formatter: (p: any) => formatDateLabel((p.data ?? p.value)?.[0] ?? ''),
        },
      },
    ],
  })
  calendarChart.off('click')
  calendarChart.on('click', (params: any) => {
    const dateStr = params?.data?.[0]
    if (dateStr) {
      selectedDateStr.value = dateStr
      detailVisible.value = true
    }
  })
}

const selectedRecord = computed(() => {
  const str = selectedDateStr.value
  if (!str) return null
  return history.value.find((h: any) => {
    const d = h.checkDate
    return d === str || (typeof d === 'string' && d.startsWith(str))
  }) ?? null
})

const isTodayCheckedIn = computed(() => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const todayStr = `${y}-${m}-${d}`
  return history.value.some((h: any) => {
    const cd = h.checkDate
    return cd === todayStr || (typeof cd === 'string' && cd.startsWith(todayStr))
  })
})

const load = async () => {
  loading.value = true
  try {
    history.value = await http.get('/api/checkin/history')
    calendarDays.value = await http.get('/api/checkin/calendar', {
      params: { year: year.value, month: month.value },
    })
    buildCalendar()
  } finally {
    loading.value = false
  }
}

const changeMonth = async (delta: number) => {
  const date = new Date(year.value, month.value - 1 + delta, 1)
  year.value = date.getFullYear()
  month.value = date.getMonth() + 1
  await load()
}

const openCheckInDialog = () => {
  checkInWeight.value = ''
  checkInFileList.value = []
  checkInDialogVisible.value = true
}

const onCheckInFileChange = (_file: UploadFile, files: UploadFile[]) => {
  checkInFileList.value = files
}

const onCheckInFileRemove = () => {
  checkInFileList.value = []
}

const submitCheckIn = async () => {
  try {
    const formData = new FormData()
    const w = checkInWeight.value?.trim()
    if (w) {
      const num = parseFloat(w)
      if (!Number.isNaN(num)) formData.append('weightKg', String(num))
    }
    const file = checkInFileList.value[0]?.raw
    if (file) formData.append('file', file)

    await http.post('/api/checkin', formData)
    checkInDialogVisible.value = false
    checkInWeight.value = ''
    checkInFileList.value = []
    checkInUploadRef.value?.clearFiles()
    await load()
    ElMessage.success('打卡成功')
  } catch (e: any) {
    ElMessage.error(e?.message || '打卡失败')
  }
}

const closeCheckInDialog = () => {
  checkInDialogVisible.value = false
  checkInWeight.value = ''
  checkInFileList.value = []
  checkInUploadRef.value?.clearFiles()
}

onMounted(load)
</script>

<template>
  <div class="layout">
    <div class="card">
      <div class="header">
        <div>
          <div class="title">今日打卡</div>
          <p class="hint">完成训练后点击打卡，可在弹窗中选填当前体重与上传健身图片，体重将同步到个人信息。</p>
        </div>
        <button class="btn" :disabled="isTodayCheckedIn" @click="openCheckInDialog">
          {{ isTodayCheckedIn ? '今日已打卡' : '打卡' }}
        </button>
      </div>
      <el-dialog
        v-model="checkInDialogVisible"
        title="打卡"
        width="420px"
        align-center
        :close-on-click-modal="true"
        @close="closeCheckInDialog"
      >
        <div class="checkin-dialog-form">
          <div class="form-row">
            <label class="form-label">当前体重 (kg)</label>
            <el-input
              v-model="checkInWeight"
              type="number"
              placeholder="选填，将同步到个人信息"
              clearable
              class="form-input-el"
            />
          </div>
          <div class="form-row upload-row">
            <label class="form-label">健身图片</label>
            <el-upload
              ref="checkInUploadRef"
              :auto-upload="false"
              :limit="1"
              accept="image/*"
              list-type="picture-card"
              :on-change="onCheckInFileChange"
              :on-remove="onCheckInFileRemove"
              :file-list="checkInFileList"
            >
              <span class="upload-text">点击上传</span>
            </el-upload>
          </div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <button class="btn btn-cancel" @click="closeCheckInDialog">取消</button>
            <button class="btn" @click="submitCheckIn">确认打卡</button>
          </span>
        </template>
      </el-dialog>
    </div>

    <div class="card">
      <div class="calendar-header">
        <div class="title">打卡日历</div>
        <div class="month-switch">
          <button class="mini-btn" @click="changeMonth(-1)">&lt;</button>
          <span>{{ year }} 年 {{ month }} 月</span>
          <button class="mini-btn" @click="changeMonth(1)">&gt;</button>
        </div>
      </div>
      <div ref="calendarDom" class="calendar"></div>
      <p class="calendar-hint">绿色格子表示当日已完成训练打卡，点击格子可查看当日详情。</p>
      <el-dialog
        v-model="detailVisible"
        title="当日打卡详情"
        width="400px"
        align-center
        @close="selectedDateStr = ''"
      >
        <template v-if="selectedRecord">
          <p class="detail-row"><span class="detail-label">日期</span> {{ selectedDateStr }}（{{ formatDateLabel(selectedDateStr) }}）</p>
          <p class="detail-row">
            <span class="detail-label">体重</span>
            {{ selectedRecord.weightKg != null ? selectedRecord.weightKg + ' kg' : '未填写' }}
          </p>
          <p class="detail-row" v-if="selectedRecord.imageUrl">
            <span class="detail-label">健身图</span>
          </p>
          <img
            v-if="selectedRecord.imageUrl"
            :src="selectedRecord.imageUrl"
            alt="健身图"
            class="detail-image"
          />
          <p v-if="!selectedRecord.imageUrl" class="detail-row"><span class="detail-label">健身图</span> 未上传</p>
        </template>
        <p v-else class="detail-empty">该日未打卡</p>
      </el-dialog>
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

.checkin-dialog-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.checkin-dialog-form .form-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.checkin-dialog-form .form-label {
  font-size: 13px;
  color: #9ca3af;
  min-width: 100px;
}

.checkin-dialog-form .form-input-el {
  flex: 1;
  max-width: 180px;
}

.checkin-dialog-form .upload-row {
  align-items: flex-start;
}

.checkin-dialog-form .upload-row .form-label {
  padding-top: 8px;
}

.upload-text {
  font-size: 12px;
  color: #9ca3af;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-cancel {
  background: transparent !important;
  border: 1px solid rgba(148, 163, 184, 0.5);
  color: #9ca3af;
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

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.month-switch {
  display: flex;
  align-items: center;
  gap: 6px;
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

.calendar {
  margin-top: 4px;
  height: 260px;
}

.calendar-hint {
  margin-top: 6px;
  font-size: 11px;
  color: #9ca3af;
}

.detail-row {
  margin: 8px 0;
  font-size: 13px;
}

.detail-label {
  color: #9ca3af;
  margin-right: 8px;
}

.detail-image {
  max-width: 100%;
  border-radius: 8px;
  margin-top: 4px;
}

.detail-empty {
  color: #9ca3af;
  font-size: 13px;
}

@media (max-width: 960px) {
  .layout {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 600px) {
  .card {
    padding: 14px 14px;
  }
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .btn {
    align-self: stretch;
    text-align: center;
  }
  .checkin-dialog-form .form-row {
    flex-direction: column;
    align-items: flex-start;
  }
  .checkin-dialog-form .form-input-el {
    max-width: 100%;
  }
  .calendar {
    height: 230px;
  }
}
</style>

