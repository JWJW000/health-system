<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import http from '../../api/http'

interface TrainAction {
  id?: number
  muscleGroup: string
  name: string
  description?: string
  setsSuggest?: number
  repsSuggest?: number
  weightSuggest?: string
}

const loading = ref(false)
const list = ref<TrainAction[]>([])
const editing = ref<TrainAction | null>(null)

const form = reactive<TrainAction>({
  muscleGroup: 'chest',
  name: '',
  description: '',
  setsSuggest: undefined,
  repsSuggest: undefined,
  weightSuggest: '',
})

const resetForm = () => {
  form.id = undefined
  form.muscleGroup = 'chest'
  form.name = ''
  form.description = ''
  form.setsSuggest = undefined
  form.repsSuggest = undefined
  form.weightSuggest = ''
}

const load = async () => {
  loading.value = true
  try {
    list.value = await http.get('/api/admin/train/actions')
  } finally {
    loading.value = false
  }
}

const edit = (item?: TrainAction) => {
  if (item) {
    editing.value = item
    Object.assign(form, item)
  } else {
    editing.value = null
    resetForm()
  }
}

const save = async () => {
  const payload: TrainAction = { ...form }
  if (!payload.name || !payload.muscleGroup) {
    alert('请填写肌群与动作名称')
    return
  }
  if (payload.id) {
    await http.put(`/api/admin/train/actions/${payload.id}`, payload)
  } else {
    await http.post('/api/admin/train/actions', payload)
  }
  await load()
  edit()
}

const remove = async (item: TrainAction) => {
  if (!item.id) return
  if (!confirm(`确定要删除动作「${item.name}」吗？`)) return
  await http.delete(`/api/admin/train/actions/${item.id}`)
  await load()
}

onMounted(load)
</script>

<template>
  <div class="admin-layout">
    <div class="header">
      <div>
        <h2 class="title">训练动作库管理</h2>
        <p class="subtitle">维护胸背腿肩核心等肌群的标准动作库</p>
      </div>
      <button class="primary" @click="edit()">新建动作</button>
    </div>

    <div class="grid">
      <div class="card list-card">
        <div class="card-title">当前动作（{{ list.length }} 条）</div>
        <div v-if="loading" class="hint">加载中...</div>
        <div v-else-if="list.length === 0" class="hint">暂无数据，请先新建动作。</div>
        <div v-else class="items">
          <div v-for="item in list" :key="item.id" class="item">
            <div class="item-main">
              <div class="name">
                {{ item.name }}
                <span class="tag">{{ item.muscleGroup }}</span>
              </div>
              <div class="meta">
                <span v-if="item.setsSuggest">{{ item.setsSuggest }} 组</span>
                <span v-if="item.repsSuggest"> · 每组 {{ item.repsSuggest }} 次</span>
                <span v-if="item.weightSuggest"> · 重量：{{ item.weightSuggest }}</span>
              </div>
              <p v-if="item.description" class="desc">{{ item.description }}</p>
            </div>
            <div class="item-actions">
              <button class="ghost" @click="edit(item)">编辑</button>
              <button class="danger" @click="remove(item)">删除</button>
            </div>
          </div>
        </div>
      </div>

      <div class="card form-card">
        <div class="card-title">{{ editing ? '编辑动作' : '新建动作' }}</div>
        <form class="form" @submit.prevent="save">
          <div class="field-row">
            <div class="field">
              <label>肌群</label>
              <select v-model="form.muscleGroup">
                <option value="chest">胸（chest）</option>
                <option value="back">背（back）</option>
                <option value="leg">腿（leg）</option>
                <option value="shoulder">肩（shoulder）</option>
                <option value="core">核心（core）</option>
                <option value="other">其他（other）</option>
              </select>
            </div>
            <div class="field">
              <label>动作名称</label>
              <input v-model="form.name" placeholder="例如：杠铃卧推 / 硬拉 / 深蹲" />
            </div>
          </div>
          <div class="field-row">
            <div class="field">
              <label>推荐组数</label>
              <input v-model.number="form.setsSuggest" type="number" min="1" />
            </div>
            <div class="field">
              <label>每组次数</label>
              <input v-model.number="form.repsSuggest" type="number" min="1" />
            </div>
          </div>
          <div class="field">
            <label>重量建议（可选）</label>
            <input v-model="form.weightSuggest" placeholder="例如：自选中等重量 / 1RM 的 60% 等" />
          </div>
          <div class="field">
            <label>动作说明与注意事项</label>
            <textarea
              v-model="form.description"
              rows="4"
              placeholder="描述动作要点、呼吸节奏、常见错误等"
            />
          </div>
          <div class="actions">
            <button type="submit" class="primary">
              {{ form.id ? '保存修改' : '创建动作' }}
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
  background: rgba(59, 130, 246, 0.18);
  color: #bfdbfe;
}

.meta {
  margin-top: 4px;
  font-size: 11px;
  color: #9ca3af;
}

.desc {
  margin-top: 4px;
  font-size: 12px;
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
</style>

