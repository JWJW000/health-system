<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const loading = ref(false)
const summary = ref<{ total?: number; recent?: any[] }>({})
const bmr = ref<number | null>(null)
const totalCalorie = ref<number | null>(null)
const macros = ref<{ carbGrams?: number; proteinGrams?: number; fatGrams?: number }>({})
const todayDiet = ref<any | null>(null)
const todayDietIndex = ref(0)
const todayTrain = ref<{ isTrainDay?: boolean; primaryGroup?: string | null }>({})
const weekCheckinCount = ref<number | null>(null)
const weekSummaryText = ref('')

const fetchData = async () => {
  try {
    loading.value = true
    // 打卡概览
    summary.value = await http.get('/api/checkin/summary')

    // 今日训练计划（简版）
    try {
      todayTrain.value = await http.get('/api/train/today-plan')
    } catch (e) {
      todayTrain.value = {}
    }

    // 最近 7 天打卡统计
    try {
      const history = await http.get('/api/checkin/history')
      if (Array.isArray(history)) {
        const todayDate = new Date()
        const sevenDaysAgo = new Date()
        sevenDaysAgo.setDate(todayDate.getDate() - 6)
        const count = history.filter((item: any) => {
          const d = new Date(item.checkDate)
          return d >= sevenDaysAgo && d <= todayDate
        }).length
        weekCheckinCount.value = count
        if (count >= 4) {
          weekSummaryText.value = `本周你已经完成 ${count} 次打卡，坚持得很好，继续保持当前节奏。`
        } else if (count >= 2) {
          weekSummaryText.value = `本周已打卡 ${count} 次，建议再安排 1-2 次训练，效果会更稳定。`
        } else {
          weekSummaryText.value = '本周打卡次数较少，建议至少安排 3 次训练，配合饮食更容易看到变化。'
        }
      }
    } catch {
      weekCheckinCount.value = null
      weekSummaryText.value = ''
    }

    // 用户档案 + 营养计算
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
      totalCalorie.value = resp.totalCalorie
      macros.value = {
        carbGrams: resp.carbGrams,
        proteinGrams: resp.proteinGrams,
        fatGrams: resp.fatGrams,
      }

      // 今日饮食计划
      try {
        const dietResp = await http.get('/api/diet/today')
        todayDiet.value = dietResp
        todayDietIndex.value = 0
      } catch {
        todayDiet.value = null
      }
    } else {
      todayDiet.value = null
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const switchDietPlan = () => {
  if (!todayDiet.value || !Array.isArray(todayDiet.value.templates) || todayDiet.value.templates.length === 0) {
    return
  }
  todayDietIndex.value = (todayDietIndex.value + 1) % todayDiet.value.templates.length
  const current = todayDiet.value.templates[todayDietIndex.value]
  todayDiet.value.template = current.template
  todayDiet.value.meals = current.meals
  todayDiet.value.shoppingList = current.shoppingList
}

const markDietExecuted = async () => {
  try {
    await http.post('/api/diet/execute-today', null, {
      params: { followed: 1 },
    })
    ElMessage.success('已记录：今天大致按推荐饮食执行')
  } catch (e: any) {
    ElMessage.error(e?.message || '记录失败')
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
        <div class="stat" v-if="totalCalorie">
          <div class="stat-label">今日推荐总热量</div>
          <div class="stat-value">
            {{ totalCalorie }}
            <span class="stat-unit">kcal</span>
          </div>
        </div>
      </div>
      <div v-if="macros.proteinGrams" class="macro-row">
        <span>推荐三大营养素：</span>
        <span>蛋白 {{ macros.proteinGrams }} g</span>
        <span>碳水 {{ macros.carbGrams }} g</span>
        <span>脂肪 {{ macros.fatGrams }} g</span>
      </div>
      <p v-if="weekSummaryText" class="week-text">
        {{ weekSummaryText }}
        <a href="/weekly-report" class="week-link">查看本周周报</a>
      </p>
    </div>

    <div class="card">
      <div class="card-title">今日训练与打卡</div>
      <p class="hint" v-if="todayTrain.isTrainDay">
        今日为 <strong>训练日</strong>，推荐主练肌群：
        <span class="pill" v-if="todayTrain.primaryGroup">
          {{
            todayTrain.primaryGroup === 'chest'
              ? '胸部'
              : todayTrain.primaryGroup === 'back'
                ? '背部'
                : todayTrain.primaryGroup === 'leg'
                  ? '腿部'
                  : todayTrain.primaryGroup === 'shoulder'
                    ? '肩部'
                    : todayTrain.primaryGroup === 'core'
                      ? '核心'
                      : todayTrain.primaryGroup
          }}
        </span>
      </p>
      <p class="hint" v-else>
        今日为 <strong>休息/机动日</strong>，可安排轻度活动或拉伸恢复。
      </p>
      <div class="sub-section">
        <div class="sub-label">最近打卡</div>
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

    <div class="card diet-card" v-if="todayDiet && todayDiet.template">
      <div class="card-title">今日饮食建议</div>
      <div class="diet-header">
        <div class="diet-name">{{ todayDiet.template.name }}</div>
        <div class="diet-meta">
          {{ todayDiet.template.totalCalorie }} kcal · 碳水 {{ todayDiet.template.carbGrams }}g ·
          蛋白 {{ todayDiet.template.proteinGrams }}g · 脂肪 {{ todayDiet.template.fatGrams }}g
        </div>
      </div>
      <div class="diet-meals">
        <div v-if="todayDiet.meals?.breakfast" class="meal-block">
          <div class="meal-title">早餐</div>
          <p class="diet-text">
            {{ todayDiet.meals.breakfast }}
          </p>
        </div>
        <div v-if="todayDiet.meals?.lunch" class="meal-block">
          <div class="meal-title">午餐</div>
          <p class="diet-text">
            {{ todayDiet.meals.lunch }}
          </p>
        </div>
        <div v-if="todayDiet.meals?.dinner" class="meal-block">
          <div class="meal-title">晚餐</div>
          <p class="diet-text">
            {{ todayDiet.meals.dinner }}
          </p>
        </div>
        <div v-if="todayDiet.meals?.snack" class="meal-block">
          <div class="meal-title">加餐</div>
          <p class="diet-text">
            {{ todayDiet.meals.snack }}
          </p>
        </div>
      </div>
      <p v-if="todayDiet.template.cookingTips" class="diet-tips">
        烹饪建议：{{ todayDiet.template.cookingTips }}
      </p>
      <div v-if="(todayDiet.shoppingList || []).length" class="shopping">
        <div class="shopping-title">本日采买清单（概览）</div>
        <ul class="shopping-list">
          <li v-for="(item, idx) in todayDiet.shoppingList" :key="idx">
            {{ item }}
          </li>
        </ul>
      </div>
      <p class="diet-note">
        {{ todayDiet.note }}
      </p>
      <div class="diet-actions">
        <button
          v-if="todayDiet.templates && todayDiet.templates.length > 1"
          class="ghost-btn"
          type="button"
          @click="switchDietPlan"
        >
          换一个方案
        </button>
        <button class="primary-btn" type="button" @click="markDietExecuted">
          今天大致按这个吃了
        </button>
      </div>
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
  gap: 24px;
  flex-wrap: wrap;
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

.macro-row {
  margin-top: 10px;
  font-size: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  color: #e5e7eb;
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

.hint {
  font-size: 12px;
  color: #9ca3af;
  margin: 0 0 8px;
}

.sub-section {
  margin-top: 8px;
}

.sub-label {
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 4px;
}

.pill {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 999px;
  border: 1px solid rgba(148, 163, 184, 0.6);
  font-size: 11px;
  margin-left: 4px;
}

.diet-card {
  grid-column: 1 / -1;
}

.diet-header {
  margin-bottom: 6px;
}

.diet-name {
  font-size: 14px;
  font-weight: 600;
}

.diet-meta {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 2px;
}

.diet-text {
  margin-top: 6px;
  font-size: 12px;
  color: #e5e7eb;
  white-space: pre-wrap;
}

.diet-meals {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 8px 12px;
  margin-top: 6px;
}

.meal-block {
  padding: 6px 8px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(55, 65, 81, 0.9);
}

.meal-title {
  font-size: 11px;
  font-weight: 600;
  margin-bottom: 2px;
}

.diet-tips {
  margin-top: 4px;
  font-size: 12px;
  color: #c4b5fd;
}

.diet-note {
  margin-top: 6px;
  font-size: 11px;
  color: #9ca3af;
}

.shopping {
  margin-top: 6px;
  font-size: 12px;
}

.shopping-title {
  font-weight: 500;
  margin-bottom: 2px;
}

.shopping-list {
  margin: 0;
  padding-left: 18px;
}

.diet-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.primary-btn {
  border-radius: 999px;
  border: none;
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
  background: linear-gradient(90deg, #22c55e, #3b82f6);
  color: #f9fafb;
}

.ghost-btn {
  border-radius: 999px;
  border: 1px solid rgba(75, 85, 99, 0.9);
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
  background: transparent;
  color: #e5e7eb;
}

@media (max-width: 960px) {
  .grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 600px) {
  .card {
    padding: 14px 14px;
  }
  .stats {
    gap: 12px;
  }
  .stat-value {
    font-size: 22px;
  }
  .diet-meals {
    grid-template-columns: minmax(0, 1fr);
  }
  .diet-actions {
    flex-direction: column;
    align-items: stretch;
  }
  .primary-btn,
  .ghost-btn {
    width: 100%;
    text-align: center;
  }
}
</style>

