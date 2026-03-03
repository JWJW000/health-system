<script setup lang="ts">
import { reactive, ref } from 'vue'
import http from '../api/http'
import { setToken, setUser } from '../utils/auth'
import { useRouter } from 'vue-router'

const router = useRouter()

const mode = ref<'login' | 'register'>('login')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

const loading = ref(false)
const errorMsg = ref('')

const switchMode = (m: 'login' | 'register') => {
  mode.value = m
  errorMsg.value = ''
}

const handleSubmit = async () => {
  if (!form.username || !form.password) {
    errorMsg.value = '请填写用户名和密码'
    return
  }
  if (mode.value === 'register' && form.password !== form.confirmPassword) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    if (mode.value === 'register') {
      await http.post('/api/auth/register', {
        username: form.username,
        password: form.password,
      })
      mode.value = 'login'
    } else {
      const resp = await http.post('/api/auth/login', {
        username: form.username,
        password: form.password,
      })
      setToken(resp.token)
      setUser({ username: resp.username || form.username, role: resp.role })
      router.push('/')
    }
  } catch (e: any) {
    errorMsg.value = e?.message || '请求失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-shell">
    <div class="panel">
      <div class="tabs">
        <button
          class="tab"
          :class="{ active: mode === 'login' }"
          @click="switchMode('login')"
        >
          登录
        </button>
        <button
          class="tab"
          :class="{ active: mode === 'register' }"
          @click="switchMode('register')"
        >
          注册
        </button>
      </div>

      <form class="form" @submit.prevent="handleSubmit">
        <div class="field">
          <label>用户名</label>
          <input v-model="form.username" placeholder="请输入用户名" />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" />
        </div>
        <div v-if="mode === 'register'" class="field">
          <label>确认密码</label>
          <input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" />
        </div>
        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
        <button type="submit" class="submit" :disabled="loading">
          {{ loading ? '处理中...' : mode === 'login' ? '登录' : '注册' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at top, #1f2937, #020617);
}

.panel {
  width: 360px;
  padding: 20px 22px;
  border-radius: 20px;
  background: radial-gradient(circle at top left, rgba(15, 23, 42, 0.98), rgba(15, 23, 42, 0.9));
  border: 1px solid rgba(148, 163, 184, 0.5);
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.9);
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.tab {
  flex: 1;
  border-radius: 999px;
  border: 1px solid rgba(55, 65, 81, 0.9);
  background: transparent;
  color: #e5e7eb;
  padding: 6px 0;
  font-size: 13px;
  cursor: pointer;
}

.tab.active {
  background: linear-gradient(90deg, #22c55e, #3b82f6);
  border-color: transparent;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 4px;
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

.error {
  font-size: 12px;
  color: #f97373;
}

.submit {
  margin-top: 4px;
  border-radius: 999px;
  border: none;
  padding: 8px 0;
  font-size: 14px;
  background: linear-gradient(90deg, #22c55e, #3b82f6);
  color: #f9fafb;
  cursor: pointer;
}

@media (max-width: 600px) {
  .login-shell {
    padding: 0 16px;
  }

  .panel {
    width: 100%;
    padding: 18px 16px;
  }
}
</style>

