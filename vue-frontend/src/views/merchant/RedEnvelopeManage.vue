<template>
  <el-container style="min-height:100vh">
    <el-header class="mh"><span>CloudMall 商家后台</span>
      <div><router-link to="/merchant/dashboard">看板</router-link><router-link to="/merchant/products">商品</router-link><router-link to="/merchant/orders">订单</router-link><router-link to="/merchant/coupons">优惠券</router-link><router-link to="/merchant/seckill">秒杀</router-link><router-link to="/merchant/groupbuy">拼团</router-link><router-link to="/merchant/presale">预售</router-link><router-link to="/merchant/distribution">分销</router-link><router-link to="/merchant/combo">套餐</router-link><router-link to="/merchant/redenvelope">红包</router-link><router-link to="/" style="color:#ff4d4f">回前台</router-link></div>
    </el-header>
    <el-main style="max-width:900px;margin:0 auto;padding:20px">
      <el-card shadow="hover" style="margin-bottom:20px">
        <h3>发送订单返利红包</h3>
        <p style="color:#999">给已完成订单的用户发红包，促进复购</p>
        <el-form :model="form" label-width="100px" style="margin-top:16px">
          <el-form-item label="订单ID"><el-input v-model="form.orderId" placeholder="输入已完成订单ID"/></el-form-item>
          <el-form-item label="金额(元)"><el-input-number v-model="form.amount" :min="1" :max="1000" :precision="2"/></el-form-item>
          <el-form-item label="祝福语"><el-input v-model="form.message" placeholder="感谢您的购买！"/></el-form-item>
          <el-form-item><el-button type="danger" @click="doSend">发送红包</el-button></el-form-item>
        </el-form>
      </el-card>
    </el-main>
  </el-container>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { sendRedEnvelope } from '@/api/redenvelope'
import { ElMessage } from 'element-plus'

const form = ref({ orderId: '', amount: 20, message: '感谢您的购买！' })
async function doSend() {
  try {
    await sendRedEnvelope({ orderId: Number(form.value.orderId), amount: Math.round(form.value.amount * 100), type: 2, message: form.value.message })
    ElMessage.success('红包已发送')
  } catch {}
}
</script>
<style scoped>
.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
.mh a{margin:0 10px;color:#666;text-decoration:none;font-size:14px;font-weight:400}
</style>
