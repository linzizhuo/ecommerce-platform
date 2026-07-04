<template>
  <AdminLayout title="数据统计">
    <el-tabs v-model="activeTab">
      <!-- 数据概览 Tab -->
      <el-tab-pane label="📈 数据概览" name="overview">
        <el-row :gutter="20" style="margin-top:10px">
          <el-col :span="4"><el-card class="sc" style="background:linear-gradient(135deg,#667eea,#764ba2)"><div class="sv">{{ today.orderCount || 0 }}</div><div class="sl">今日订单</div></el-card></el-col>
          <el-col :span="4"><el-card class="sc" style="background:linear-gradient(135deg,#f093fb,#f5576c)"><div class="sv">¥{{ fmtMoney(today.payAmount) }}</div><div class="sl">今日营收</div></el-card></el-col>
          <el-col :span="4"><el-card class="sc" style="background:linear-gradient(135deg,#4facfe,#00f2fe)"><div class="sv">{{ today.newUsers || 0 }}</div><div class="sl">今日新用户</div></el-card></el-col>
          <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#43e97b,#38f9d7)"><div class="sv">{{ weekTotal.pv }}</div><div class="sl">近7天PV</div></el-card></el-col>
          <el-col :span="6"><el-card class="sc" style="background:linear-gradient(135deg,#fa709a,#fee140)"><div class="sv">{{ weekTotal.uv }}</div><div class="sl">近7天UV</div></el-card></el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top:20px">
          <el-col :span="12"><el-card><template #header><span class="card-title">📊 订单 & 营收趋势（近30天）</span></template><div ref="orderChartRef" style="height:350px"></div></el-card></el-col>
          <el-col :span="12"><el-card><template #header><span class="card-title">📈 流量趋势 PV/UV（近7天）</span></template><div ref="trafficChartRef" style="height:350px"></div></el-card></el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top:20px">
          <el-col :span="12"><el-card><template #header><span class="card-title">👤 新增用户趋势</span></template><div ref="userChartRef" style="height:320px"></div></el-card></el-col>
          <el-col :span="12"><el-card><template #header><span class="card-title">🥧 订单状态分布</span></template><div ref="pieChartRef" style="height:320px"></div></el-card></el-col>
        </el-row>
        <el-card style="margin-top:20px"><template #header>📋 近7天每日明细</template>
          <el-table :data="weekData" style="width:100%">
            <el-table-column prop="statDate" label="日期" width="120" />
            <el-table-column prop="orderCount" label="订单数" />
            <el-table-column label="订单金额"><template #default="{row}">¥{{ fmtMoney(row.orderAmount) }}</template></el-table-column>
            <el-table-column prop="payCount" label="支付笔数" />
            <el-table-column label="支付金额"><template #default="{row}">¥{{ fmtMoney(row.payAmount) }}</template></el-table-column>
            <el-table-column prop="newUserCount" label="新增用户" />
            <el-table-column prop="visitCount" label="PV" />
            <el-table-column prop="visitUserCount" label="UV" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 用户画像 Tab -->
      <el-tab-pane label="👤 用户画像" name="userProfile">
        <el-row :gutter="20" style="margin-top:10px">
          <el-col :span="12"><el-card><template #header>💰 消费层级分布</template><div ref="spendTierRef" style="height:300px"></div></el-card></el-col>
          <el-col :span="12"><el-card><template #header>🛒 客单价分布</template><div ref="avgOrderRef" style="height:300px"></div></el-card></el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top:20px">
          <el-col :span="8">
            <el-card class="sc" style="background:linear-gradient(135deg,#667eea,#764ba2)"><div class="sv">{{ profile.repurchaseRate || 0 }}%</div><div class="sl">复购率</div></el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="sc" style="background:linear-gradient(135deg,#43e97b,#38f9d7)"><div class="sv">{{ profile.totalUsersWithOrders || 0 }}</div><div class="sl">有订单用户数</div></el-card>
          </el-col>
        </el-row>
        <el-card style="margin-top:20px"><template #header>🏷️ 品类偏好 Top5</template>
          <el-table :data="profile.favCategories || []"><el-table-column prop="categoryName" label="品类"/><el-table-column prop="soldCount" label="销量"/></el-table>
        </el-card>
      </el-tab-pane>

      <!-- 营销效果 Tab -->
      <el-tab-pane label="🎯 营销效果" name="marketing">
        <el-row :gutter="20" style="margin-top:10px">
          <el-col :span="12"><el-card><template #header>🎫 优惠券统计</template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="发放量">{{ mkt.coupon?.totalIssued || 0 }}</el-descriptions-item>
              <el-descriptions-item label="领取量">{{ mkt.coupon?.totalReceived || 0 }}</el-descriptions-item>
              <el-descriptions-item label="核销量">{{ mkt.coupon?.totalUsed || 0 }}</el-descriptions-item>
              <el-descriptions-item label="核销率"><span style="color:#ff4d4f;font-weight:700">{{ mkt.coupon?.usageRate || 0 }}%</span></el-descriptions-item>
            </el-descriptions>
          </el-card></el-col>
          <el-col :span="12"><el-card><template #header>⚡ 秒杀统计</template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="秒杀场次">{{ mkt.seckill?.sessionCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="总库存">{{ mkt.seckill?.totalStock || 0 }}</el-descriptions-item>
              <el-descriptions-item label="已售">{{ mkt.seckill?.soldCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="售罄率"><span style="color:#ff4d4f;font-weight:700">{{ mkt.seckill?.sellThroughRate || 0 }}%</span></el-descriptions-item>
            </el-descriptions>
          </el-card></el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top:20px">
          <el-col :span="12"><el-card><template #header>👥 拼团统计</template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="总团数">{{ mkt.groupBuy?.totalGroups || 0 }}</el-descriptions-item>
              <el-descriptions-item label="成团数">{{ mkt.groupBuy?.successCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="失败数">{{ mkt.groupBuy?.failCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="成团率"><span style="color:#ff4d4f;font-weight:700">{{ mkt.groupBuy?.successRate || 0 }}%</span></el-descriptions-item>
            </el-descriptions>
          </el-card></el-col>
          <el-col :span="12"><el-card><template #header>🎪 活动统计</template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="活动总数">{{ mkt.activity?.totalActivities || 0 }}</el-descriptions-item>
              <el-descriptions-item label="进行中">{{ mkt.activity?.activeCount || 0 }}</el-descriptions-item>
            </el-descriptions>
          </el-card></el-col>
        </el-row>
      </el-tab-pane>

      <!-- 流量分析 Tab -->
      <el-tab-pane label="📊 流量分析" name="traffic">
        <el-row :gutter="20" style="margin-top:10px">
          <el-col :span="12"><el-card><template #header>🔍 热门搜索词 Top10</template>
            <el-table :data="traffic.hotWords || []"><el-table-column prop="keyword" label="关键词"/><el-table-column prop="searchCount" label="搜索次数"/></el-table>
          </el-card></el-col>
          <el-col :span="12"><el-card><template #header>🏆 商品销量排行 Top10</template>
            <el-table :data="traffic.topProducts || []"><el-table-column prop="name" label="商品"/><el-table-column prop="salesCount" label="销量"/></el-table>
          </el-card></el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import AdminLayout from '@/layouts/AdminLayout.vue'

const activeTab = ref('overview')
const today = ref<any>({})
const weekData = ref<any[]>([])
const monthData = ref<any[]>([])
const statusDist = ref<Record<number, number>>({})
const profile = ref<any>({})
const mkt = ref<any>({})
const traffic = ref<any>({})

const orderChartRef = ref<HTMLElement>()
const trafficChartRef = ref<HTMLElement>()
const userChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
const spendTierRef = ref<HTMLElement>()
const avgOrderRef = ref<HTMLElement>()

const statusLabels: Record<number, string> = { 0:'待付款', 1:'待发货', 2:'待收货', 3:'已完成', 4:'已取消', 5:'退款中', 6:'已退款' }

function fmtMoney(v: any): string { const n = Number(v) || 0; return (n / 100).toFixed(0) }

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
    tooltip: { trigger: 'axis' }, legend: { data: ['订单数', '营收(元)'] },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: 30, fontSize: 10 } },
    yAxis: [{ type: 'value', name: '订单数' }, { type: 'value', name: '营收(元)', axisLabel: { formatter: (v: number) => '¥' + (v / 100).toFixed(0) } }],
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
    tooltip: { trigger: 'axis' }, legend: { data: ['PV', 'UV'] },
    xAxis: { type: 'category', data: dates }, yAxis: { type: 'value', name: '访问量' },
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
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: monthData.value.map((d: any) => d.statDate), axisLabel: { rotate: 30, fontSize: 10 } },
    yAxis: { type: 'value', name: '新用户数' },
    series: [{ name: '新增用户', type: 'bar', data: monthData.value.map((d: any) => d.newUserCount || 0), itemStyle: { color: '#fa709a', borderRadius: [4, 4, 0, 0] } }],
    grid: { left: 50, right: 20, top: 20, bottom: 50 }
  })
}

function initPieChart() {
  if (!pieChartRef.value) return
  const chart = echarts.init(pieChartRef.value)
  const data = Object.entries(statusDist.value).map(([k, v]) => ({ name: statusLabels[Number(k)] || '状态' + k, value: v }))
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{ type: 'pie', radius: ['40%', '70%'], center: ['50%', '50%'], data, label: { formatter: '{b}\n{d}%' } }]
  })
}

function initProfileCharts() {
  if (spendTierRef.value && profile.value.spendTier) {
    const chart = echarts.init(spendTierRef.value)
    const entries = Object.entries(profile.value.spendTier)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: ['40%', '70%'], data: entries.map(([k, v]) => ({ name: k, value: v })), label: { formatter: '{b}\n{d}%' } }]
    })
  }
  if (avgOrderRef.value && profile.value.avgOrderTier) {
    const chart = echarts.init(avgOrderRef.value)
    const entries = Object.entries(profile.value.avgOrderTier)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: entries.map(([k]) => k) },
      yAxis: { type: 'value', name: '订单数' },
      series: [{ type: 'bar', data: entries.map(([, v]) => v), itemStyle: { color: '#667eea', borderRadius: [4, 4, 0, 0] } }],
      grid: { left: 50, right: 20, top: 10, bottom: 40 }
    })
  }
}

async function loadOverview() {
  try { const [tRes,wRes,mRes,sRes]=await Promise.all([request.get('/statistics/today'),request.get('/statistics/week'),request.get('/statistics/month'),request.get('/statistics/order-status-distribution')]);today.value=(tRes as any).data||{};weekData.value=(wRes as any).data||[];monthData.value=(mRes as any).data||[];statusDist.value=(sRes as any).data||{} }catch{}
  await nextTick();initOrderChart();initTrafficChart();initUserChart();initPieChart()
}

async function loadProfile() {
  try { const r:any=await request.get('/statistics/user-profile');profile.value=r.data||{} }catch{}
  await nextTick();initProfileCharts()
}

async function loadMarketing() { try { const r:any=await request.get('/statistics/marketing');mkt.value=r.data||{} }catch{} }
async function loadTraffic() { try { const r:any=await request.get('/statistics/traffic-analysis');traffic.value=r.data||{} }catch{} }

watch(activeTab, (tab) => {
  if (tab === 'userProfile' && !Object.keys(profile.value).length) loadProfile()
  if (tab === 'marketing' && !Object.keys(mkt.value).length) loadMarketing()
  if (tab === 'traffic' && !Object.keys(traffic.value).length) loadTraffic()
})

onMounted(loadOverview)
</script>

<style scoped>
.sc{border-radius:12px;color:#fff;border:none;text-align:center;padding:10px}
.sv{font-size:36px;font-weight:700}
.sl{font-size:13px;opacity:.9;margin-top:4px}
.card-title{font-weight:700;font-size:15px}
</style>
