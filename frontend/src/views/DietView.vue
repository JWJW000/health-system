<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '../api/http'

const loading = ref(false)
const diets = ref<any[]>([])

const load = async () => {
  loading.value = true
  try {
    diets.value = await http.get('/api/diet/list')
  } finally {
    loading.value = false
  }
}

const toggleCollect = async (item: any) => {
  const payload = { dietId: item.id }
  try {
    if (item.collected) {
      await http.delete('/api/diet/collect', { data: payload })
      item.collected = false
    } else {
      await http.post('/api/diet/collect', payload)
      item.collected = true
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(load)
</script>

<template>
  <div class="cards">
    <div v-for="d in diets" :key="d.id" class="card">
      <div class="card-header">
        <div>
          <div class="name">{{ d.name }}</div>
          <div class="meta">
            {{ d.totalCalorie }} kcal · 碳水 {{ d.carbGrams }}g · 蛋白 {{ d.proteinGrams }}g · 脂肪
            {{ d.fatGrams }}g
          </div>
        </div>
        <button class="collect" @click="toggleCollect(d)">
          {{ d.collected ? '已收藏' : '收藏' }}
        </button>
      </div>
      <div class="body">
        <p class="section-title">每日三餐 + 加餐</p>
        <p class="text">{{ d.mealPlan }}</p>
        <p v-if="d.cookingTips" class="section-title">烹饪建议</p>
        <p v-if="d.cookingTips" class="text">{{ d.cookingTips }}</p>
      </div>
    </div>
    <div v-if="!loading && diets.length === 0" class="empty">暂无推荐食谱，请先完善个人信息。</div>
  </div>
</template>

<style scoped>
.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

.card {
  background: radial-gradient(circle at top left, rgba(15, 23, 42, 0.95), rgba(15, 23, 42, 0.8));
  border-radius: 18px;
  padding: 14px 16px;
  border: 1px solid rgba(148, 163, 184, 0.35);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.name {
  font-size: 14px;
  font-weight: 600;
}

.meta {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 4px;
}

.collect {
  border-radius: 999px;
  border: none;
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
  background: linear-gradient(90deg, #22c55e, #3b82f6);
  color: #f9fafb;
}

.body {
  font-size: 12px;
  color: #e5e7eb;
}

.section-title {
  margin: 6px 0 2px;
  font-weight: 500;
}

.text {
  white-space: pre-wrap;
  color: #d1d5db;
}

.empty {
  font-size: 13px;
  color: #9ca3af;
}
</style>

