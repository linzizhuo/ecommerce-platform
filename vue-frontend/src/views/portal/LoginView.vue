<template>
  <div class="login-page">
    <el-card class="form-card">
      <h2>用户登录</h2>
      <el-form @submit.prevent="handleLogin">
        <el-form-item><el-input v-model="phone" placeholder="手机号" size="large" /></el-form-item>
        <el-form-item><el-input v-model="password" type="password" placeholder="密码" size="large" /></el-form-item>
        <el-form-item><el-button type="danger" size="large" @click="handleLogin" :loading="loading" style="width:100%">登录</el-button></el-form-item>
      </el-form>
      <p style="text-align:center;color:#999">还没有账号？<router-link to="/register">立即注册</router-link></p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()
const phone = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    await userStore.login(phone.value, password.value)
    ElMessage.success('登录成功')
    router.push('/')
  } catch { /* handled by interceptor */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #ff4d4f, #667eea); }
.form-card { width: 400px; }
.form-card h2 { text-align: center; margin-bottom: 30px; }
</style>
