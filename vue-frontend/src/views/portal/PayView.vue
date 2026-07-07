<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">💳 支付</div>
    </div></header>
    <div class="container">
      <el-card v-if="order" class="pay-card">
        <h2>订单支付</h2>
        <el-descriptions :column="2" border style="margin:20px 0">
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="应付金额"><span style="color:#ff4d4f;font-size:18px;font-weight:700">¥{{ (order.payAmount/100).toFixed(2) }}</span></el-descriptions-item>
          <el-descriptions-item label="商品详情">{{ order.items?.map((i:any)=>i.productName).join(', ') }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>选择支付方式</el-divider>
        <div style="display:flex;gap:16px;justify-content:center;margin:20px 0">
          <el-card :class="['pay-method', payMethod===1?'selected':'']" @click="payMethod=1" shadow="hover">
            <div style="text-align:center;font-size:48px">💙</div>
            <div style="text-align:center;margin-top:8px;font-weight:700">支付宝</div>
          </el-card>
          <el-card :class="['pay-method', payMethod===2?'selected':'']" @click="payMethod=2" shadow="hover">
            <div style="text-align:center;font-size:48px">💚</div>
            <div style="text-align:center;margin-top:8px;font-weight:700">微信支付</div>
          </el-card>
        </div>
        <el-button type="danger" size="large" @click="doPay" :loading="paying" style="width:100%;margin-top:16px">
          💰 确认支付 ¥{{ (order.payAmount/100).toFixed(2) }}
        </el-button>
      </el-card>
      <div v-if="paid" style="text-align:center;margin-top:40px">
        <el-result icon="success" title="支付成功" sub-title="订单已支付，商家将尽快发货">
          <template #extra>
            <el-button type="primary" @click="$router.push('/orders')">查看订单</el-button>
            <el-button @click="$router.push('/')">继续购物</el-button>
          </template>
        </el-result>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const order = ref<any>(null)
const payMethod = ref(1)
const paying = ref(false)
const paid = ref(false)

async function load() {
  const id = route.params.orderId
  const r: any = await request.get('/order/' + id)
  // 后端返回 { order, items, logistics, payment }，展平到 order 对象上
  const data = r.data
  order.value = { ...data.order, items: data.items, logistics: data.logistics, payment: data.payment }
}
async function doPay() {
  paying.value = true
  try {
    // 模拟支付回调
    await request.post('/payment/callback', {
      orderNo: order.value.orderNo,
      payNo: 'PAY' + Date.now(),
      amount: order.value.payAmount,
      payMethod: payMethod.value
    })
    ElMessage.success('支付成功！')
    paid.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '支付失败')
  } finally { paying.value = false }
}
onMounted(load)
</script>
<style scoped>
.page{background:#f0f2f5;min-height:100vh}
.top-bar{background:#fff;box-shadow:0 1px 4px rgba(0,0,0,.06)}
.inner{max-width:1200px;margin:0 auto;padding:0 20px}
.top-row{display:flex;align-items:center;gap:16px;height:60px}
.logo{font-size:22px;font-weight:700;color:#ff4d4f;text-decoration:none}
.title{font-size:20px;font-weight:700;flex:1}
.container{max-width:700px;margin:0 auto;padding:20px}
.pay-card{border-radius:12px}
.pay-method{cursor:pointer;width:160px;text-align:center;border:2px solid transparent;border-radius:12px}
.pay-method.selected{border-color:#ff4d4f;background:#fff1f0}
.pay-method:hover{border-color:#ff7875}
</style>
