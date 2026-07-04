<template>
  <AdminLayout title="数据大屏">
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#667eea,#764ba2)"><div class="sv">{{ data.userCount }}</div><div class="sl">注册用户</div></el-card></el-col>
      <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#f093fb,#f5576c)"><div class="sv">{{ data.productCount }}</div><div class="sl">商品总数</div></el-card></el-col>
      <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#4facfe,#00f2fe)"><div class="sv">{{ data.orderCount }}</div><div class="sl">订单总数</div></el-card></el-col>
      <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#43e97b,#38f9d7)"><div class="sv">¥{{ (data.revenue/100).toFixed(0) }}</div><div class="sl">平台营收</div></el-card></el-col>
    </el-row>
  </AdminLayout>
</template>
<script setup lang="ts">
import { reactive, onMounted } from 'vue'; import request from '@/utils/request'
import AdminLayout from '@/layouts/AdminLayout.vue'
const data = reactive({ userCount:0, productCount:0, orderCount:0, revenue:0 })
onMounted(async () => { try { const r:any = await request.get('/admin/dashboard'); Object.assign(data, r.data) } catch{} })
</script>
<style scoped>.sc{border-radius:12px;color:#fff;border:none;text-align:center;padding:10px}.sv{font-size:36px;font-weight:700}.sl{font-size:13px;opacity:.9;margin-top:4px}</style>
