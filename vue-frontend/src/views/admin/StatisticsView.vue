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
        <el-menu-item index="/admin/dict">📋 数据字典</el-menu-item>
        <el-menu-item index="/admin/violations">⚠️ 违规处罚</el-menu-item>
        <el-menu-item index="/admin/config">⚙️ 系统配置</el-menu-item>
      </el-menu>
    </el-aside>
    <el-main class="main">
      <h2>📈 数据分析中心</h2>

      <!-- 概览卡片 -->
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="4"><el-card class="sc" style="background:linear-gradient(135deg,#667eea,#764ba2)"><div class="sv">{{ today.orderCount || 0 }}</div><div class="sl">今日订单</div></el-card></el-col>
        <el-col :span="4"><el-card class="sc" style="background:linear-gradient(135deg,#f093fb,#f5576c)"><div class="sv">¥{{ fmtMoney(today.payAmount) }}</div><div class="sl">今日营收</div></el-card></el-col>
        <el-col :span="4"><el-card class="sc" style="background:linear-gradient(135deg,#4facfe,#00f2fe)"><div class="sv">{{ today.newUsers || 0 }}</div><div class="sl">今日新用户</div></el-card></el-col>
        <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#43e97b,#38f9d7)"><div class="sv">{{ weekTotal.pv }}</div><div class="sl">近7天PV</div></el-card></el-col>
        <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#fa709a,#fee140)"><div class="sv">{{ weekTotal.uv }}</div><div class="sl">近7天UV</div></el-card></el-col>
      </el-row>

      <!-- 趋势图表 -->
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="12">
          <el-card><template #header><span class="card-title">📊 订单 & 营收趋势（近30天）</span></template>
            <div ref="orderChartRef" style="height:350px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card><template #header><span class="card-title">📈 流量趋势 PV/UV（近7天）</span></template>
            <div ref="trafficChartRef" style="height:350px"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="12">
          <el-card><template #header><span class="card-title">👤 新增用户趋势（近30天）</span></template>
            <div ref="userChartRef" style="height:320px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card><template #header><span class="card-title">🥧 订单状态分布</span></template>
            <div ref="pieChartRef" style="height:320px"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 数据表格 -->
      <el-card style="margin-top:20px">
        <template #header><span class="card-title">📋 近7天每日明细</span></template>
        <el-table :data="weekData" style="width:100%">
          <el-table-column prop="statDate" label="日期" width="120" />
          <el-table-column prop="orderCount" label="订单数" />
          <el-table-column label="订单金额"><template #default="{row}">¥{{ fmtMoney(row.orderAmount) }}</template></el-table-column>
          <el-table-column prop="payCount" label="支付笔数" />
          <el-table-column label="支付金额"><template #default="{row}">¥{{ fmtMoney(row.payAmount) }}</template></el-table-column>
          <el-table-column prop="newUserCount" label="新增用户" />
          <el-table-column prop="visitCount" label="访问量(PV)" />
          <el-table-column prop="visitUserCount" label="访客数(UV)" />
        </el-table>
      </el-card>
    </el-main>
  </el-container></div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const today = ref<any>({})
const weekData = ref<any[]>([])
const monthData = ref<any[]>([])
const statusDist = ref<Record<number, number>>({})

const orderChartRef = ref<HTMLElement>()
const trafficChartRef = ref<HTMLElement>()
const userChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()

const statusLabels: Record<number, string> = { 0:'待付款', 1:'待发货', 2:'待收货', 3:'已完成', 4:'已取消', 5:'退款中', 6:'已退款' }

function fmtMoney(v: any): string {
  const n = Number(v) || 0
  return (n / 100).toFixed(0)
}

const weekTotal = computed(() => {
  let pv = 0, uv = 0
  for (const d of weekData.value) { pv += (d.visitCount || 0); uv += (d.visitUserCount || 0) }
  return { pv, uv }
})

function initOrderChart() {
  if (!orderChartRef.value) return
  const chart = echarts.init(orderChartRef.value)
  const dates = monthData.value.map((d: any) => d.statDate)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '营收(元)'] },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: 30, fontSize: 10 } },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '营收(元)', axisLabel: { formatter: (v: number) => '¥' + (v / 100).toFixed(0) } }
    ],
    series: [
      { name: '订单数', type: 'line', data: monthData.value.map((d: any) => d.orderCount || 0), smooth: true, itemStyle: { color: '#667eea' } },
      { name: '营收(元)', type: 'line', yAxisIndex: 1, data: monthData.value.map((d: any) => (d.payAmount || 0) / 100), smooth: true, itemStyle: { color: '#f5576c' } }
    ],
    grid: { left: 50, right: 60, top: 20, bottom: 50 }
  })
}

function initTrafficChart() {
  if (!trafficChartRef.value) return
  const chart = echarts.init(trafficChartRef.value)
  const dates = weekData.value.map((d: any) => d.statDate)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['PV', 'UV'] },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '访问量' },
    series: [
      { name: 'PV', type: 'bar', data: weekData.value.map((d: any) => d.visitCount || 0), itemStyle: { color: '#4facfe' }, barGap: '10%' },
      { name: 'UV', type: 'bar', data: weekData.value.map((d: any) => d.visitUserCount || 0), itemStyle: { color: '#43e97b' } }
    ],
    grid: { left: 50, right: 20, top: 20, bottom: 30 }
  })
}

function initUserChart() {
  if (!userChartRef.value) return
  const chart = echarts.init(userChartRef.value)
  const dates = monthData.value.map((d: any) => d.statDate)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: 30, fontSize: 10 } },
    yAxis: { type: 'value', name: '新用户数' },
    series: [{
      name: '新增用户', type: 'bar',
      data: monthData.value.map((d: any) => d.newUserCount || 0),
      itemStyle: { color: '#fa709a', borderRadius: [4, 4, 0, 0] }
    }],
    grid: { left: 50, right: 20, top: 20, bottom: 50 }
  })
}

function initPieChart() {
  if (!pieChartRef.value) return
  const chart = echarts.init(pieChartRef.value)
  const data = Object.entries(statusDist.value).map(([k, v]) => ({
    name: statusLabels[Number(k)] || '状态' + k,
    value: v
  }))
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['50%', '50%'],
      data,
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } },
      label: { formatter: '{b}\n{d}%' }
    }]
  })
}

async function loadData() {
  try {
    const [tRes, wRes, mRes, sRes] = await Promise.all([
      request.get('/statistics/today'),
      request.get('/statistics/week'),
      request.get('/statistics/month'),
      request.get('/statistics/order-status-distribution')
    ])
    today.value = (tRes as any).data || {}
    weekData.value = (wRes as any).data || []
    monthData.value = (mRes as any).data || []
    statusDist.value = (sRes as any).data || {}
  } catch {}
  await nextTick()
  initOrderChart(); initTrafficChart(); initUserChart(); initPieChart()
}

onMounted(loadData)
</script>

<style scoped>
.page{min-height:100vh;background:#f0f2f5}
.aside{background:#304156;min-height:100vh;padding:20px 0}
.aside h3{text-align:center;color:#fff;margin-bottom:20px;font-size:20px}
.main{background:#f0f2f5;min-height:100vh;padding:24px}
.sc{border-radius:12px;color:#fff;border:none;text-align:center;padding:10px}
.sv{font-size:36px;font-weight:700}
.sl{font-size:13px;opacity:.9;margin-top:4px}
.card-title{font-weight:700;font-size:15px}
</style>
