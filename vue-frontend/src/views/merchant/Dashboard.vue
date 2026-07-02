<template>
  <div class="page"><el-container>
    <el-header class="mh"><span>CloudMall 商家后台</span>
      <div><router-link to="/merchant/dashboard">看板</router-link><router-link to="/merchant/products">商品</router-link><router-link to="/merchant/orders">订单</router-link><router-link to="/merchant/coupons">优惠券</router-link><router-link to="/merchant/seckill">秒杀</router-link><router-link to="/merchant/groupbuy">拼团</router-link><router-link to="/merchant/presale">预售</router-link><router-link to="/merchant/distribution">分销</router-link><router-link to="/merchant/combo">套餐</router-link><router-link to="/merchant/redenvelope">红包</router-link><router-link to="/merchant/activities">活动</router-link><router-link to="/merchant/reconciliation">对账</router-link><router-link to="/merchant/stock-report">库存</router-link><router-link to="/" style="color:#ff4d4f">回前台</router-link></div>
    </el-header>
    <el-main>
      <h3>数据看板</h3>
      <!-- 概览卡片 -->
      <el-row :gutter="20" style="margin-top:16px">
        <el-col :span="4"><el-card class="sc" style="background:linear-gradient(135deg,#667eea,#764ba2)"><div class="sv">{{ data.productCount }}</div><div class="sl">商品总数</div></el-card></el-col>
        <el-col :span="4"><el-card class="sc" style="background:linear-gradient(135deg,#f093fb,#f5576c)"><div class="sv">{{ data.orderCount }}</div><div class="sl">总订单</div></el-card></el-col>
        <el-col :span="4"><el-card class="sc" style="background:linear-gradient(135deg,#4facfe,#00f2fe)"><div class="sv">{{ data.todayOrders }}</div><div class="sl">今日订单</div></el-card></el-col>
        <el-col :span="4"><el-card class="sc" style="background:linear-gradient(135deg,#43e97b,#38f9d7)"><div class="sv">¥{{ fmtMoney(data.revenue) }}</div><div class="sl">营收(元)</div></el-card></el-col>
        <el-col :span="4"><el-card class="sc" style="background:linear-gradient(135deg,#fa709a,#fee140)"><div class="sv">{{ data.visitUserCount || 0 }}</div><div class="sl">访客(UV)</div></el-card></el-col>
        <el-col :span="4"><el-card class="sc" :style="'background:linear-gradient(135deg,'+(data.conversionRate>5?'#f093fb,#f5576c':'#667eea,#764ba2')+')'"><div class="sv">{{ data.conversionRate || 0 }}%</div><div class="sl">转化率</div></el-card></el-col>
      </el-row>
      <!-- 库存预警 -->
      <el-row v-if="data.lowStockCount > 0" style="margin-top:12px">
        <el-alert :title="'库存预警：' + data.lowStockCount + ' 个SKU库存不足（<10）'" type="warning" show-icon :closable="false">
          <template #default><el-button size="small" type="warning" @click="$router.push('/merchant/stock-report')">查看详情</el-button></template>
        </el-alert>
      </el-row>
      <!-- 趋势图表 -->
      <el-card style="margin-top:20px">
        <template #header><span class="card-title">📊 订单 & 营收趋势（近7天）</span></template>
        <div ref="chartRef" style="height:350px"></div>
      </el-card>
    </el-main>
  </el-container></div>
</template>
<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const data = reactive<any>({ productCount:0, orderCount:0, todayOrders:0, revenue:0, visitCount:0, visitUserCount:0, conversionRate:0, lowStockCount:0, trend:[] })
const chartRef = ref<HTMLElement>()

function fmtMoney(v: any) { return ((Number(v) || 0) / 100).toFixed(0) }

function initChart() {
  if (!chartRef.value || !data.trend?.length) return
  const chart = echarts.init(chartRef.value)
  const dates = data.trend.map((d: any) => d.statDate)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '营收(元)', '访客'] },
    xAxis: { type: 'category', data: dates },
    yAxis: [
      { type: 'value', name: '数量' },
      { type: 'value', name: '营收(元)', axisLabel: { formatter: (v: number) => '¥' + (v / 100).toFixed(0) } }
    ],
    series: [
      { name: '订单数', type: 'line', data: data.trend.map((d: any) => d.orderCount || 0), smooth: true, itemStyle: { color: '#667eea' } },
      { name: '营收(元)', type: 'line', yAxisIndex: 1, data: data.trend.map((d: any) => (d.payAmount || 0) / 100), smooth: true, itemStyle: { color: '#f5576c' } },
      { name: '访客', type: 'bar', data: data.trend.map((d: any) => d.visitCount || 0), itemStyle: { color: '#4facfe' }, barGap: '10%' }
    ],
    grid: { left: 50, right: 60, top: 20, bottom: 30 }
  })
}

onMounted(async () => {
  try { const r: any = await request.get('/merchant/dashboard'); Object.assign(data, r.data) } catch {}
  await nextTick(); initChart()
})
</script>
<style scoped>
.page{min-height:100vh;background:#f0f2f5}
.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
.mh a{margin:0 6px;color:#666;text-decoration:none;font-size:13px;font-weight:400}
.mh a.router-link-active{color:#1890ff}
.el-main{max-width:1200px;margin:20px auto}
.sc{border-radius:12px;color:#fff;border:none;text-align:center;padding:10px}
.sv{font-size:30px;font-weight:700}
.sl{font-size:13px;opacity:.9;margin-top:4px}
.card-title{font-weight:700;font-size:15px}
</style>
