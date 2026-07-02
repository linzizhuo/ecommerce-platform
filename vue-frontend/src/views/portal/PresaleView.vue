<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">📦 预售活动</div>
      <router-link to="/" class="back">返回首页</router-link>
    </div></header>
    <div class="container">
      <div class="banner">📦 预售专区 · 定金锁价 · 尾款发货</div>
      <div v-if="list.length" class="presale-grid">
        <el-card v-for="p in list" :key="p.id" shadow="hover" class="presale-card">
          <h3>{{ p.productName || '商品#' + p.productId }}</h3>
          <p class="ps-price">定金: ¥{{ (p.deposit / 100).toFixed(2) }}</p>
          <p class="ps-final">尾款: ¥{{ (p.finalAmount / 100).toFixed(2) }}</p>
          <p class="ps-total">总价: ¥{{ ((p.deposit + p.finalAmount) / 100).toFixed(2) }}</p>
          <p class="ps-phase">定金期: {{ p.depositStart }} ~ {{ p.depositEnd }}</p>
          <p class="ps-phase">尾款期: {{ p.finalStart }} ~ {{ p.finalEnd }}</p>
          <el-button type="warning" :disabled="!isDepositPhase(p)" @click="payDeposit(p.id)">付定金</el-button>
          <el-button type="primary" :disabled="!isFinalPhase(p)" @click="payFinal(p.id)" style="margin-left:8px">付尾款</el-button>
        </el-card>
      </div>
      <el-empty v-else description="暂无预售活动" />
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPresaleList, payDeposit as apiPayDeposit, payFinal as apiPayFinal } from '@/api/presale'
import { ElMessage } from 'element-plus'

const list = ref<any[]>([])
async function load() { const r: any = await getPresaleList(); list.value = r.data || [] }
function isDepositPhase(p: any) {
  if (!p.depositStart || !p.depositEnd) return false
  const now = new Date()
  return now >= new Date(p.depositStart) && now <= new Date(p.depositEnd)
}
function isFinalPhase(p: any) {
  if (!p.finalStart || !p.finalEnd) return false
  const now = new Date()
  return now >= new Date(p.finalStart) && now <= new Date(p.finalEnd)
}
async function payDeposit(id: number) {
  try { await apiPayDeposit(id); ElMessage.success('定金支付成功'); load() } catch {}
}
async function payFinal(id: number) {
  try { await apiPayFinal(id); ElMessage.success('尾款支付成功'); load() } catch {}
}
onMounted(load)
</script>
<style scoped>
.page{background:#f0f2f5;min-height:100vh}
.top-bar{background:#fff;box-shadow:0 1px 4px rgba(0,0,0,0.06);position:sticky;top:0;z-index:100}
.inner{max-width:1200px;margin:0 auto;padding:0 20px}
.top-row{display:flex;align-items:center;gap:24px;height:60px}
.logo{font-size:22px;font-weight:700;color:#ff4d4f;text-decoration:none}
.title{font-size:20px;font-weight:700;color:#333;flex:1}
.back{color:#999;text-decoration:none;font-size:14px}
.container{max-width:1200px;margin:0 auto;padding:20px}
.banner{background:linear-gradient(135deg,#faad14,#ffc53d);color:#fff;padding:30px;border-radius:12px;font-size:24px;font-weight:700;text-align:center;margin-bottom:20px}
.presale-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:18px}
.presale-card{border-radius:12px;text-align:center;padding:20px}
.ps-price{color:#faad14;font-size:18px;font-weight:700}
.ps-final{color:#1677ff;font-size:16px}
.ps-total{color:#333;font-size:16px;font-weight:700;margin-top:4px}
.ps-phase{color:#999;font-size:12px;margin:4px 0}
</style>
