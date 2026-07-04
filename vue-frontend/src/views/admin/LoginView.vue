<template>
  <div class="login-page">
    <el-card class="form-card"><h2>平台管理登录</h2>
      <el-input v-model="username" placeholder="管理员账号" size="large" style="margin-bottom:15px" />
      <el-input v-model="password" type="password" placeholder="密码" size="large" style="margin-bottom:15px" />
      <el-button type="primary" size="large" @click="login" style="width:100%">登录</el-button>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'; import { useRouter } from 'vue-router'; import { ElMessage } from 'element-plus'
import request from '@/utils/request'
const router = useRouter(); const username = ref(''); const password = ref(''); const loading = ref(false)
async function login() {
  loading.value = true
  try {
    const res: any = await request.post('/admin/login', { username: username.value, password: password.value })
    localStorage.setItem('token', res.data)
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } finally { loading.value = false }
}
</script>
<style scoped>.login-page{min-height:100vh;display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,#304156,#1890ff)}.form-card{width:400px}h2{text-align:center;margin-bottom:20px}</style>
