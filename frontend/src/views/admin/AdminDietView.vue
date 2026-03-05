<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import http from '../../api/http'

interface DietTemplate {
  id?: number
  fitnessType: number
  name: string
  totalCalorie: number
  carbGrams: number
  proteinGrams: number
  fatGrams: number
  mealPlan: string
  cookingTips?: string
  tags?: string
  suitableLevel?: string
  estimatedCostLevel?: string
}

const loading = ref(false)
const list = ref<DietTemplate[]>([])
const editing = ref<DietTemplate | null>(null)

const form = reactive<DietTemplate>({
  fitnessType: 1,
  name: '',
  totalCalorie: 0,
  carbGrams: 0,
  proteinGrams: 0,
  fatGrams: 0,
  mealPlan: '',
  cookingTips: '',
  tags: '',
  suitableLevel: 'NEWBIE',
  estimatedCostLevel: 'MEDIUM',
})

const resetForm = () => {
  form.id = undefined
  form.fitnessType = 1
  form.name = ''
  form.totalCalorie = 0
  form.carbGrams = 0
  form.proteinGrams = 0
  form.fatGrams = 0
  form.mealPlan = ''
  form.cookingTips = ''
  form.tags = ''
  form.suitableLevel = 'NEWBIE'
  form.estimatedCostLevel = 'MEDIUM'
}

const load = async () => {
  loading.value = true
  try {
    list.value = await http.get('/api/admin/diet/templates')
  } finally {
    loading.value = false
  }
}

const edit = (item?: DietTemplate) => {
  if (item) {
    editing.value = item
    Object.assign(form, item)
  } else {
    editing.value = null
    resetForm()
  }
}

const save = async () => {
  const payload = { ...form }
  if (!payload.name || !payload.mealPlan) {
    alert('请填写名称和每日三餐方案')
    return
  }
  if (payload.id) {
    await http.put(`/api/admin/diet/templates/${payload.id}`, payload)
  } else {
    await http.post('/api/admin/diet/templates', payload)
  }
  await load()
  edit()
}

const remove = async (item: DietTemplate) => {
  if (!item.id) return
  if (!confirm(`确定要删除食谱「${item.name}」吗？`)) return
  await http.delete(`/api/admin/diet/templates/${item.id}`)
  await load()
}

onMounted(load)
</script>

<template>
  <div class="admin-layout">
    <div class="header">
      <div>
        <h2 class="title">食谱模板管理</h2>
        <p class="subtitle">维护不同健身目标下的标准饮食方案</p>
      </div>
      <button class="primary" @click="edit()">新建食谱</button>
    </div>

    <div class="grid">
      <div class="card list-card">
        <div class="card-title">当前模板（{{ list.length }} 条）</div>
        <div v-if="loading" class="hint">加载中...</div>
        <div v-else-if="list.length === 0" class="hint">暂无数据，请先新建模板。</div>
        <div v-else class="items">
          <div v-for="item in list" :key="item.id" class="item">
            <div class="item-main">
              <div class="name">
                {{ item.name }}
                <span class="tag">
                  {{
                    item.fitnessType === 1
                      ? '增肌'
                      : item.fitnessType === 2
                        ? '减脂'
                        : item.fitnessType === 3
                          ? '塑形'
                          : '功能性'
                  }}
                </span>
              </div>
              <div class="meta">
                {{ item.totalCalorie }} kcal · 碳水 {{ item.carbGrams }}g · 蛋白 {{ item.proteinGrams }}g · 脂肪
                {{ item.fatGrams }}g
                <span v-if="item.suitableLevel" class="pill">
                  {{ item.suitableLevel === 'NEWBIE' ? '新手' : item.suitableLevel === 'INTERMEDIATE' ? '进阶' : '高级' }}
                </span>
              </div>
            </div>
            <div class="item-actions">
              <button class="ghost" @click="edit(item)">编辑</button>
              <button class="danger" @click="remove(item)">删除</button>
            </div>
          </div>
        </div>
      </div>

      <div class="card form-card">
        <div class="card-title">{{ editing ? '编辑食谱' : '新建食谱' }}</div>
        <form class="form" @submit.prevent="save">
          <div class="field">
            <label>适配目标</label>
            <select v-model.number="form.fitnessType">
              <option :value="1">增肌</option>
              <option :value="2">减脂</option>
              <option :value="3">塑形</option>
              <option :value="4">功能性训练</option>
            </select>
          </div>
          <div class="field">
            <label>名称</label>
            <input v-model="form.name" placeholder="例如：增肌·高蛋白均衡套餐" />
          </div>
          <div class="field-row">
            <div class="field">
              <label>总热量 (kcal)</label>
              <input v-model.number="form.totalCalorie" type="number" min="0" />
            </div>
            <div class="field">
              <label>碳水 (g)</label>
              <input v-model.number="form.carbGrams" type="number" min="0" step="0.1" />
            </div>
          </div>
          <div class="field-row">
            <div class="field">
              <label>蛋白质 (g)</label>
              <input v-model.number="form.proteinGrams" type="number" min="0" step="0.1" />
            </div>
            <div class="field">
              <label>脂肪 (g)</label>
              <input v-model.number="form.fatGrams" type="number" min="0" step="0.1" />
            </div>
          </div>
          <div class="field">
            <label>每日三餐 + 加餐</label>
            <textarea
              v-model="form.mealPlan"
              rows="4"
              placeholder="按早/中/晚餐 + 加餐描述，如：&#10;早餐：燕麦牛奶 + 水煮蛋&#10;午餐：鸡胸肉 + 糙米饭 + 蔬菜&#10;晚餐：..."
            />
          </div>
          <div class="field">
            <label>烹饪建议（可选）</label>
            <textarea v-model="form.cookingTips" rows="3" placeholder="如：少油少盐、优先蒸煮炖等" />
          </div>
          <div class="field">
            <label>标签（逗号分隔，如：上班族,低预算,中餐）</label>
            <input v-model="form.tags" placeholder="上班族,低预算,中餐" />
          </div>
          <div class="field-row">
            <div class="field">
              <label>适合水平</label>
              <select v-model="form.suitableLevel">
                <option value="NEWBIE">新手</option>
                <option value="INTERMEDIATE">进阶</option>
                <option value="ADVANCED">高级</option>
              </select>
            </div>
            <div class="field">
              <label>预算档位</label>
              <select v-model="form.estimatedCostLevel">
                <option value="LOW">低</option>
                <option value="MEDIUM">中</option>
                <option value="HIGH">高</option>
              </select>
            </div>
          </div>
          <div class="actions">
            <button type="submit" class="primary">
              {{ form.id ? '保存修改' : '创建模板' }}
            </button>
            <button type="button" class="ghost" @click="edit()">重置表单</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1.4fr);
  gap: 16px;
}

.card {
  background: radial-gradient(circle at top left, rgba(15, 23, 42, 0.96), rgba(15, 23, 42, 0.88));
  border-radius: 18px;
  padding: 16px 18px;
  border: 1px solid rgba(148, 163, 184, 0.35);
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}

.hint {
  font-size: 12px;
  color: #9ca3af;
}

.items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(55, 65, 81, 0.9);
}

.item-main {
  flex: 1;
}

.name {
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tag {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 999px;
  background: rgba(34, 197, 94, 0.15);
  color: #bbf7d0;
}

.meta {
  margin-top: 4px;
  font-size: 11px;
  color: #9ca3af;
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

label {
  font-size: 11px;
  color: #9ca3af;
}

input,
select,
textarea {
  border-radius: 12px;
  border: 1px solid rgba(55, 65, 81, 0.9);
  background: rgba(15, 23, 42, 0.9);
  color: #e5e7eb;
  padding: 6px 9px;
  font-size: 12px;
  resize: vertical;
}

input:focus,
select:focus,
textarea:focus {
  outline: none;
  border-color: #22c55e;
}

button {
  border-radius: 999px;
  border: none;
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
}

.primary {
  background: linear-gradient(90deg, #22c55e, #3b82f6);
  color: #f9fafb;
}

.ghost {
  background: transparent;
  border: 1px solid rgba(75, 85, 99, 0.9);
  color: #e5e7eb;
}

.danger {
  background: rgba(248, 113, 113, 0.12);
  border: 1px solid rgba(248, 113, 113, 0.9);
  color: #fecaca;
}

.actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 6px;
}

@media (max-width: 960px) {
  .grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 600px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .header .primary {
    align-self: stretch;
    text-align: center;
  }
  .item {
    flex-direction: column;
    align-items: flex-start;
  }
  .item-actions {
    flex-direction: row;
    align-self: stretch;
    justify-content: flex-end;
  }
  .field-row {
    grid-template-columns: minmax(0, 1fr);
  }
  .actions {
    flex-direction: column;
    align-items: stretch;
  }
  .actions .primary,
  .actions .ghost {
    width: 100%;
    text-align: center;
  }
}
</style>

