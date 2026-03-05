<script setup lang="ts">
import { reactive, ref } from 'vue'
import http from '../api/http'

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const loading = ref(false)
const message = ref('')

const handleChangePassword = async () => {
  if (!form.oldPassword || !form.newPassword) {
    message.value = '请填写原密码和新密码'
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    message.value = '两次输入的新密码不一致'
    return
  }
  loading.value = true
  message.value = ''
  try {
    await http.post('/api/auth/change-password', {
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
    })
    message.value = '密码修改成功'
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
  } catch (e: any) {
    message.value = e?.message || '修改失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="layout">
    <div class="card">
      <div class="title">账号设置</div>
      <p class="subtitle">修改登录密码，保护你的账号安全。</p>
      <form class="form" @submit.prevent="handleChangePassword">
        <div class="field">
          <label>原密码</label>
          <input v-model="form.oldPassword" type="password" />
        </div>
        <div class="field">
          <label>新密码</label>
          <input v-model="form.newPassword" type="password" />
        </div>
        <div class="field">
          <label>确认新密码</label>
          <input v-model="form.confirmPassword" type="password" />
        </div>
        <p v-if="message" class="message">
          {{ message }}
        </p>
        <button type="submit" :disabled="loading">
          {{ loading ? '提交中...' : '修改密码' }}
        </button>
      </form>
      
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
  margin: 4px 0 12px;
  font-size: 12px;
  color: #9ca3af;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 360px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

label {
  font-size: 12px;
  color: #9ca3af;
}

input {
  border-radius: 999px;
  border: 1px solid rgba(55, 65, 81, 0.9);
  background: rgba(15, 23, 42, 0.9);
  color: #e5e7eb;
  padding: 7px 10px;
  font-size: 13px;
}

input:focus {
  outline: none;
  border-color: #22c55e;
}

button {
  margin-top: 6px;
  border-radius: 999px;
  border: none;
  padding: 8px 0;
  font-size: 14px;
  background: linear-gradient(90deg, #22c55e, #3b82f6);
  color: #f9fafb;
  cursor: pointer;
  max-width: 160px;
}

.message {
  font-size: 12px;
  color: #e5e7eb;
}

.hint {
  margin-top: 8px;
  font-size: 12px;
  color: #9ca3af;
}
</style>

