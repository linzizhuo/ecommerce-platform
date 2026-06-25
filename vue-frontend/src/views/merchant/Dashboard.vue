<template>
  <div class="page"><el-container>
    <el-header class="mh"><span>CloudMall 商家后台</span>
      <div><router-link to="/merchant/dashboard">看板</router-link><router-link to="/merchant/products">商品</router-link><router-link to="/merchant/orders">订单</router-link><router-link to="/merchant/coupons">优惠券</router-link><router-link to="/" style="color:#ff4d4f">回前台</router-link></div>
    </el-header>
    <el-main>
      <h3>数据看板</h3>
      <el-row :gutter="20">
        <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#667eea,#764ba2)"><div class="sv">{{ data.productCount }}</div><div class="sl">商品总数</div></el-card></el-col>
        <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#f093fb,#f5576c)"><div class="sv">{{ data.orderCount }}</div><div class="sl">总订单</div></el-card></el-col>
        <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#4facfe,#00f2fe)"><div class="sv">{{ data.todayOrders }}</div><div class="sl">今日订单</div></el-card></el-col>
        <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#43e97b,#38f9d7)"><div class="sv">¥{{ (data.revenue/100).toFixed(0) }}</div><div class="sl">营收(元)</div></el-card></el-col>
      </el-row>
    </el-main>
  </el-container></div>
</template>
<script setup lang="ts">
import { reactive, onMounted } from 'vue'; import request from '@/utils/request'
const data = reactive({ productCount:0, orderCount:0, todayOrders:0, revenue:0 })
onMounted(async () => { try { const r:any = await request.get('/merchant/dashboard'); Object.assign(data, r.data) } catch{} })
</script>
<style scoped>.page{min-height:100vh;background:#f0f2f5}.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}.mh a{margin:0 10px;color:#666;text-decoration:none;font-size:14px;font-weight:400}.el-main{max-width:1200px;margin:20px auto}.sc{border-radius:12px;color:#fff;border:none;text-align:center;padding:10px}.sv{font-size:36px;font-weight:700}.sl{font-size:13px;opacity:.9;margin-top:4px}</style>
