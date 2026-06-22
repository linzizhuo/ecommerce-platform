<template>
  <div class="login-page">
    <el-card class="form-card">
      <h2>用户注册</h2>
      <el-form @submit.prevent>
        <el-form-item><el-input v-model="phone" placeholder="手机号" size="large" /></el-form-item>
        <el-form-item><el-input v-model="nickname" placeholder="昵称" size="large" /></el-form-item>
        <el-form-item><el-input v-model="password" type="password" placeholder="密码(至少6位)" size="large" /></el-form-item>
        <el-form-item><el-button type="danger" size="large" @click="handleRegister" :loading="loading" style="width:100%">注册</el-button></el-form-item>
      </el-form>
      <p style="text-align:center;color:#999">已有账号？<router-link to="/login">立即登录</router-link></p>
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
const phone = ref(''), nickname = ref(''), password = ref(''), loading = ref(false)
async function handleRegister() {
  if (password.value.length < 6) { ElMessage.warning('密码至少6位'); return }
  loading.value = true
  try { await userStore.register(phone.value, password.value, nickname.value); ElMessage.success('注册成功'); router.push('/') }
  finally { loading.value = false }
}
</script>
<style scoped>.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #ff4d4f, #667eea); }
.form-card { width: 400px; } .form-card h2 { text-align: center; margin-bottom: 30px; }</style>
