<script setup lang="ts">
import { reactive, ref } from 'vue'
import http from '../../api/http'
import { setToken, setUser } from '../../utils/auth'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = reactive({
  username: '',
  password: '',
})

const loading = ref(false)
const errorMsg = ref('')

const handleSubmit = async () => {
  if (!form.username || !form.password) {
    errorMsg.value = '请填写用户名和密码'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const resp = await http.post('/api/auth/login', {
      username: form.username,
      password: form.password,
    })
    if (!resp.role || resp.role !== 'ADMIN') {
      errorMsg.value = '该账号不是管理员，请使用管理员账号登录'
      return
    }
    setToken(resp.token)
    setUser({ username: resp.username || form.username, role: resp.role })
    router.push('/admin/dashboard')
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
      <h1 class="title">管理员登录</h1>

      <form class="form" @submit.prevent="handleSubmit">
        <div class="field">
          <label>管理员账号</label>
          <input v-model="form.username" placeholder="请输入管理员用户名" />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" />
        </div>
        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
        <button type="submit" class="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录后台' }}
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
  background: radial-gradient(circle at top, #020617, #020617);
}

.panel {
  width: 360px;
  padding: 22px 24px;
  border-radius: 20px;
  background: radial-gradient(circle at top left, rgba(15, 23, 42, 0.98), rgba(15, 23, 42, 0.9));
  border: 1px solid rgba(148, 163, 184, 0.5);
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.9);
}

.title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.subtitle {
  margin: 4px 0 14px;
  font-size: 12px;
  color: #9ca3af;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 10px;
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
  margin-top: 6px;
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

