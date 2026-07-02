<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">📦 物流跟踪</div>
      <router-link to="/orders" class="back">返回订单</router-link>
    </div></header>
    <div class="container" v-if="logistics">
      <el-card class="info-card">
        <p>快递公司: {{ logistics.company || '待发货' }}</p>
        <p>快递单号: {{ logistics.trackingNo || '-' }}</p>
        <p>状态: {{ ['待揽收','运输中','派送中','已签收'][logistics.status]||'未知' }}</p>
      </el-card>
      <el-card class="timeline-card" v-if="logistics.traceData">
        <el-timeline>
          <el-timeline-item v-for="(t,i) in parseTrace(logistics.traceData)" :key="i" :timestamp="t.time">
            {{ t.desc }}
          </el-timeline-item>
        </el-timeline>
      </el-card>
      <el-button v-if="logistics.status===2" type="danger" @click="confirmSign">确认收货</el-button>
    </div>
    <el-empty v-else description="暂无物流信息" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { queryLogistics, confirmReceive } from '@/api/logistics'
import { ElMessage } from 'element-plus'
const route = useRoute()
const logistics = ref<any>(null)
function parseTrace(data: string) { try { return JSON.parse(data) } catch { return [] } }
async function load() { const r:any=await queryLogistics(Number(route.params.orderId)); logistics.value=r.data }
async function confirmSign() { await confirmReceive(Number(route.params.orderId)); ElMessage.success('已确认收货'); load() }
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
.container{max-width:800px;margin:0 auto;padding:20px}
.info-card{margin-bottom:16px;border-radius:12px}
.timeline-card{border-radius:12px}
</style>
