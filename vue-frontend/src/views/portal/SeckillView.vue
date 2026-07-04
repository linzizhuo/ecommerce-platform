<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">⚡ 限时秒杀</div>
      <router-link to="/" class="back">返回首页</router-link>
    </div></header>
    <div class="container">
      <div class="banner">⚡ 秒杀专区 · 手慢无</div>
      <div v-if="sessions.length" class="seckill-grid">
        <el-card v-for="s in sessions" :key="s.id" shadow="hover" class="seckill-card">
          <div class="sk-img">⚡</div>
          <h3>SKU #{{ s.skuId }}</h3>
          <p class="sk-price">秒杀价: ¥{{ (s.seckillPrice / 100).toFixed(2) }}</p>
          <p class="sk-stock">剩余 {{ s.stock }} 件</p>
          <p class="sk-time">{{ countdown(s) }}</p>
          <el-button type="danger" :disabled="!canBuy(s)" @click="doBuy(s.id)">立即秒杀</el-button>
        </el-card>
      </div>
      <el-empty v-else description="暂无秒杀活动" />
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getSeckillSessions, doSeckill, getSeckillResult } from '@/api/seckill'
import { ElMessage } from 'element-plus'

const router = useRouter()
const sessions = ref<any[]>([])
let timer: any = null
let pollTimer: any = null

async function load() { const r: any = await getSeckillSessions(); sessions.value = r.data || [] }
function canBuy(s: any) {
  const now = new Date()
  const start = new Date(s.startTime)
  const end = new Date(s.endTime)
  return now >= start && now <= end && s.stock > 0
}
function countdown(s: any) {
  const now = new Date()
  const start = new Date(s.startTime)
  const end = new Date(s.endTime)
  if (now < start) {
    const diff = Math.floor((start.getTime() - now.getTime()) / 1000)
    const h = Math.floor(diff / 3600)
    const m = Math.floor((diff % 3600) / 60)
    const sec = diff % 60
    return `距开始: ${h}时${m}分${sec}秒`
  }
  if (now > end) return '已结束'
  const diff = Math.floor((end.getTime() - now.getTime()) / 1000)
  const h = Math.floor(diff / 3600)
  const m = Math.floor((diff % 3600) / 60)
  const sec = diff % 60
  return `距结束: ${h}时${m}分${sec}秒`
}
async function doBuy(id: number) {
  try { await doSeckill(id); ElMessage.success('抢购成功，订单生成中...'); startPolling(id) }
  catch {}
}
function startPolling(sessionId: number) {
  let count = 0
  pollTimer = setInterval(async () => {
    count++
    try {
      const r: any = await getSeckillResult(sessionId)
      const data = r.data
      if (data.status === 'SUCCESS') {
        clearInterval(pollTimer)
        ElMessage.success('订单已生成！')
        router.push(`/order/${data.orderId}`)
      } else if (data.status === 'FAILED') {
        clearInterval(pollTimer)
        ElMessage.error('订单生成失败，请查看订单列表')
      } else if (count > 20) {
        clearInterval(pollTimer)
        ElMessage.warning('订单处理中，请稍后在订单列表查看')
      }
    } catch { /* 继续轮询 */ }
  }, 1500)
}
onMounted(() => { load(); timer = setInterval(load, 5000) })
onUnmounted(() => { clearInterval(timer); clearInterval(pollTimer) })
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
.banner{background:linear-gradient(135deg,#ff4d4f,#ff7875);color:#fff;padding:30px;border-radius:12px;font-size:24px;font-weight:700;text-align:center;margin-bottom:20px}
.seckill-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:18px}
.seckill-card{border-radius:12px;text-align:center;padding:20px}
.sk-img{font-size:64px}
.sk-price{color:#ff4d4f;font-size:18px;font-weight:700}
.sk-stock{color:#999;font-size:13px}
.sk-time{color:#666;font-size:13px;margin-bottom:10px}
</style>
