<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import * as echarts from 'echarts'

const router = useRouter()

const form = reactive({
  id: undefined as number | undefined,
  username: '',
  gender: 1,
  age: 25,
  heightCm: 170,
  weightKg: 65,
  bodyFatPct: 20,
  fitnessGoal: 2,
  experienceYears: 0,
  trainingScene: '',
  allergyFoods: '',
})

const weightForm = reactive({
  weightKg: 65,
})

const saving = ref(false)
const weightSaving = ref(false)
const isNewProfile = ref(false)
const wasNewProfile = ref(false)
const step = ref(1)

const loadProfile = async () => {
  try {
    const profile = await http.get('/api/user/profile/me')
    if (profile) {
      Object.assign(form, profile)
      weightForm.weightKg = profile.weightKg
      isNewProfile.value = false
      wasNewProfile.value = false
    } else {
      // 首次使用，引导模式
      isNewProfile.value = true
      wasNewProfile.value = true
      step.value = 1
    }
  } catch (e) {
    // ignore when no profile
    isNewProfile.value = true
    wasNewProfile.value = true
    step.value = 1
  }
}

const saveProfile = async () => {
  try {
    saving.value = true
    await http.post('/api/user/profile', {
      ...form,
      id: form.id,
    })
    await loadProfile()
    if (wasNewProfile.value) {
      // 首次建档完成后，进入首页「今日概览」
      isNewProfile.value = false
      wasNewProfile.value = false
      router.push('/')
    }
  } finally {
    saving.value = false
  }
}

const saveWeight = async () => {
  try {
    weightSaving.value = true
    await http.post('/api/user/weight', {
      weightKg: weightForm.weightKg,
    })
    await loadWeightCurve()
  } finally {
    weightSaving.value = false
  }
}

const chartDom = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null

const buildChart = (dates: string[], values: number[]) => {
  if (!chartDom.value) return
  if (!chart) {
    chart = echarts.init(chartDom.value)
  }
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { color: '#9ca3af', fontSize: 10 },
      axisLine: { lineStyle: { color: '#4b5563' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#9ca3af', fontSize: 10 },
      splitLine: { lineStyle: { color: '#1f2937' } },
    },
    grid: { left: 32, right: 12, top: 24, bottom: 24 },
    series: [
      {
        data: values,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          color: '#22c55e',
          width: 2,
        },
        itemStyle: {
          color: '#22c55e',
        },
        areaStyle: {
          color: 'rgba(34,197,94,0.15)',
        },
      },
    ],
  })
}

const loadWeightCurve = async () => {
  const list = await http.get('/api/user/weight/recent', {
    params: { days: 15 },
  })
  const dates: string[] = []
  const values: number[] = []
  if (Array.isArray(list)) {
    list.forEach((item) => {
      dates.push(item.logDate)
      values.push(item.weightKg)
    })
  }
  buildChart(dates, values)
}

onMounted(async () => {
  await loadProfile()
  await loadWeightCurve()
})
</script>

<template>
  <div class="layout">
    <div class="card">
      <div class="card-title">
        基础信息
        <span v-if="isNewProfile" class="tag">首次使用向导 · 第 {{ step }} 步 / 2</span>
      </div>
      <form class="form" @submit.prevent="saveProfile">
        <!-- 第一步：必须的信息 -->
        <template v-if="!isNewProfile || step === 1">
          <div class="field">
            <label>性别</label>
            <select v-model="form.gender">
              <option :value="1">男</option>
              <option :value="2">女</option>
            </select>
          </div>
          <div class="field">
            <label>年龄</label>
            <input v-model.number="form.age" type="number" min="10" max="100" />
          </div>
          <div class="field">
            <label>身高 (cm)</label>
            <input v-model.number="form.heightCm" type="number" min="100" max="250" />
          </div>
          <div class="field">
            <label>当前体重 (kg)</label>
            <input v-model.number="form.weightKg" type="number" min="30" max="300" />
          </div>
          <div class="field">
            <label>健身目标</label>
            <select v-model="form.fitnessGoal">
              <option :value="1">增肌</option>
              <option :value="2">减脂</option>
              <option :value="3">塑形</option>
              <option :value="4">功能性训练</option>
            </select>
          </div>
        </template>

        <!-- 第二步：可选的详细信息 -->
        <template v-if="!isNewProfile || step === 2">
          <div class="field">
            <label>昵称（可选）</label>
            <input v-model="form.username" placeholder="可选填写" />
          </div>
          <div class="field">
            <label>体脂率 (%)（可选）</label>
            <input v-model.number="form.bodyFatPct" type="number" min="3" max="60" />
          </div>
          <div class="field">
            <label>健身年限（年，可选）</label>
            <input v-model.number="form.experienceYears" type="number" min="0" max="50" />
          </div>
          <div class="field">
            <label>训练场景（可选）</label>
            <input v-model="form.trainingScene" placeholder="如：健身房 / 家用器械" />
          </div>
          <div class="field">
            <label>过敏食材（可选）</label>
            <input v-model="form.allergyFoods" placeholder="如：花生、海鲜等" />
          </div>
        </template>

        <div class="actions">
          <template v-if="isNewProfile">
            <button
              v-if="step === 1"
              type="button"
              :disabled="saving"
              @click="step = 2"
            >
              下一步：补充详细信息
            </button>
            <button
              v-if="step === 2"
              type="button"
              :disabled="saving"
              @click="step = 1"
            >
              上一步
            </button>
            <button type="submit" :disabled="saving">
              {{ saving ? '保存中...' : '完成并进入今日概览' }}
            </button>
          </template>
          <template v-else>
            <button type="submit" :disabled="saving">
              {{ saving ? '保存中...' : '保存信息' }}
            </button>
          </template>
        </div>
      </form>
    </div>

    <div class="card">
      <div class="card-title">体重记录与曲线（15 天）</div>
      <form class="inline-form" @submit.prevent="saveWeight">
        <label>今日体重 (kg)</label>
        <input v-model.number="weightForm.weightKg" type="number" min="30" max="300" />
        <button type="submit" :disabled="weightSaving">
          {{ weightSaving ? '记录中...' : '记录' }}
        </button>
      </form>
      <div ref="chartDom" class="chart"></div>
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(0, 1.4fr);
  gap: 16px;
}

.card {
  background: radial-gradient(circle at top left, rgba(15, 23, 42, 0.95), rgba(15, 23, 42, 0.8));
  border-radius: 18px;
  padding: 18px 20px;
  border: 1px solid rgba(148, 163, 184, 0.35);
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

label {
  font-size: 11px;
  color: #9ca3af;
}

input,
select,
button {
  border-radius: 999px;
  border: 1px solid rgba(55, 65, 81, 0.8);
  background: rgba(15, 23, 42, 0.9);
  color: #e5e7eb;
  padding: 6px 10px;
  font-size: 12px;
}

input:focus,
select:focus {
  outline: none;
  border-color: #22c55e;
}

button {
  cursor: pointer;
  background: linear-gradient(90deg, #22c55e, #3b82f6);
  border: none;
}

.actions {
  grid-column: 1 / -1;
  margin-top: 6px;
  display: flex;
  justify-content: flex-end;
}

.inline-form {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 12px;
}

.chart {
  height: 260px;
}

@media (max-width: 960px) {
  .layout {
    grid-template-columns: minmax(0, 1fr);
  }
  .form {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 600px) {
  .card {
    padding: 14px 14px;
  }
  .inline-form {
    flex-direction: column;
    align-items: stretch;
  }
  .inline-form button {
    width: 100%;
    text-align: center;
  }
}
</style>

