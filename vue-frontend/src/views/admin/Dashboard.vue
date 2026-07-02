<template>
  <div class="page"><el-container>
    <el-aside width="220px" class="aside"><h3>CloudMall</h3>
      <el-menu :default-active="$route.path" background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF" router>
        <el-menu-item index="/admin/dashboard">📊 数据大屏</el-menu-item>
        <el-menu-item index="/admin/statistics">📈 数据统计</el-menu-item>
        <el-menu-item index="/admin/users">👤 用户管理</el-menu-item>
        <el-menu-item index="/admin/merchants">🏪 商家管理</el-menu-item>
        <el-menu-item index="/admin/products">📦 商品审核</el-menu-item>
        <el-menu-item index="/admin/roles">🔐 角色权限</el-menu-item>
        <el-menu-item index="/admin/activities">🎯 活动管理</el-menu-item>
        <el-menu-item index="/admin/venues">🏟️ 会场管理</el-menu-item>
        <el-menu-item index="/admin/dict">📋 数据字典</el-menu-item>
        <el-menu-item index="/admin/violations">⚠️ 违规处罚</el-menu-item>
        <el-menu-item index="/admin/config">⚙️ 系统配置</el-menu-item>
      </el-menu>
    </el-aside>
    <el-main class="main">
      <h2>数据大屏</h2>
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#667eea,#764ba2)"><div class="sv">{{ data.userCount }}</div><div class="sl">注册用户</div></el-card></el-col>
        <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#f093fb,#f5576c)"><div class="sv">{{ data.productCount }}</div><div class="sl">商品总数</div></el-card></el-col>
        <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#4facfe,#00f2fe)"><div class="sv">{{ data.orderCount }}</div><div class="sl">订单总数</div></el-card></el-col>
        <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#43e97b,#38f9d7)"><div class="sv">¥{{ (data.revenue/100).toFixed(0) }}</div><div class="sl">平台营收</div></el-card></el-col>
      </el-row>
    </el-main>
  </el-container></div>
</template>
<script setup lang="ts">
import { reactive, onMounted } from 'vue'; import request from '@/utils/request'
const data = reactive({ userCount:0, productCount:0, orderCount:0, revenue:0 })
onMounted(async () => { try { const r:any = await request.get('/admin/dashboard'); Object.assign(data, r.data) } catch{} })
</script>
<style scoped>.page{min-height:100vh;background:#f0f2f5}.aside{background:#304156;min-height:100vh;padding:20px 0}.aside h3{text-align:center;color:#fff;margin-bottom:20px;font-size:20px}.main{background:#f0f2f5;min-height:100vh;padding:24px}.sc{border-radius:12px;color:#fff;border:none;text-align:center;padding:10px}.sv{font-size:36px;font-weight:700}.sl{font-size:13px;opacity:.9;margin-top:4px}</style>
