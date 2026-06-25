<template>
  <div class="login-page">
    <el-card class="form-card">
      <h2>商家登录</h2>
      <el-input v-model="phone" placeholder="手机号" size="large" style="margin-bottom:15px" />
      <el-input v-model="password" type="password" placeholder="密码" size="large" style="margin-bottom:15px" />
      <el-button type="primary" size="large" @click="login" :loading="loading" style="width:100%">登录</el-button>
      <p style="text-align:center;color:#999;margin-top:15px">使用注册的手机号登录即可</p>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'; import { useRouter } from 'vue-router'; import { ElMessage } from 'element-plus'; import { useUserStore } from '@/stores/userStore'
const router = useRouter(); const userStore = useUserStore()
const phone = ref(''), password = ref(''), loading = ref(false)
async function login() {
  loading.value = true
  try { await userStore.login(phone.value, password.value); ElMessage.success('登录成功'); router.push('/merchant/dashboard') }
  finally { loading.value = false }
}
</script>
<style scoped>.login-page{min-height:100vh;display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,#1890ff,#52c41a)}.form-card{width:400px}h2{text-align:center;margin-bottom:20px}</style>
