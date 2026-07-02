<template>
  <div class="page">
    <header class="top-bar"><div class="top-inner">
      <router-link to="/" class="logo">CloudMall</router-link>
      <router-link to="/orders" class="back-link">← 返回订单列表</router-link>
    </div></header>
    <div class="content-inner" v-if="order">
      <!-- 订单状态 -->
      <el-card class="section">
        <div class="status-bar">
          <h2>订单详情</h2>
          <el-tag v-if="order.status===0" type="warning" size="large">待付款</el-tag>
          <el-tag v-else-if="order.status===1" size="large">待发货</el-tag>
          <el-tag v-else-if="order.status===2" type="primary" size="large">待收货</el-tag>
          <el-tag v-else-if="order.status===3" type="success" size="large">已完成</el-tag>
          <el-tag v-else-if="order.status===4" type="info" size="large">已取消</el-tag>
          <el-tag v-else type="info" size="large">其他</el-tag>
        </div>
      </el-card>

      <!-- 订单信息 -->
      <el-card class="section">
        <template #header><span class="card-title">📋 订单信息</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="商品总价">¥{{ ((order.totalAmount || 0) / 100).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="优惠金额">-¥{{ ((order.discountAmount || 0) / 100).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span class="pay-amount">¥{{ ((order.payAmount || 0) / 100).toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ order.payTime || '未支付' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ order.addressSnapshot || '暂无' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 商品明细 -->
      <el-card class="section">
        <template #header><span class="card-title">📦 商品明细</span></template>
        <el-table :data="items" style="width:100%">
          <el-table-column label="商品" min-width="300">
            <template #default="{row}">
              <div class="item-product">
                <div class="item-img" :style="{background: itemColor(row.skuId)}">{{ row.productName?.charAt(0) || '商' }}</div>
                <div>
                  <div class="item-name">{{ row.productName }}</div>
                  <div class="item-spec" v-if="row.specInfo">{{ formatSpec(row.specInfo) }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template #default="{row}">¥{{ ((row.price || 0) / 100).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="120">
            <template #default="{row}">¥{{ ((row.price * row.quantity || 0) / 100).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 支付信息 -->
      <el-card class="section" v-if="payment">
        <template #header><span class="card-title">💳 支付信息</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="支付单号">{{ payment.payNo }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ payMethodText(payment.payMethod) }}</el-descriptions-item>
          <el-descriptions-item label="支付金额">¥{{ ((payment.amount || 0) / 100).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ payment.payTime }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 物流信息 -->
      <el-card class="section" v-if="logistics">
        <template #header><span class="card-title">🚚 物流信息</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="快递公司">{{ logistics.company }}</el-descriptions-item>
          <el-descriptions-item label="运单号">{{ logistics.trackingNo }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ logistics.shipTime || '待发货' }}</el-descriptions-item>
          <el-descriptions-item label="签收时间">{{ logistics.signTime || '未签收' }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="logistics.traceData" class="trace-data">
          <p class="trace-title">物流轨迹</p>
          <pre>{{ logistics.traceData }}</pre>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button v-if="order.status===0" type="danger" size="large" @click="payOrder">💳 去支付</el-button>
        <el-button v-if="order.status===2" type="primary" size="large" @click="confirmReceive">✅ 确认收货</el-button>
        <el-button v-if="order.status===0" type="default" size="large" @click="cancelOrder">取消订单</el-button>
        <router-link v-if="logistics" :to="`/logistics/${order.id}`">
          <el-button type="info" size="large">📦 查看物流轨迹</el-button>
        </router-link>
      </div>
    </div>
    <div class="content-inner" v-else-if="loading">
      <el-skeleton :rows="10" animated />
    </div>
    <el-empty v-else description="订单不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const order = ref<any>(null)
const items = ref<any[]>([])
const logistics = ref<any>(null)
const payment = ref<any>(null)
const loading = ref(true)

const itemColors = ['linear-gradient(135deg,#667eea,#764ba2)','linear-gradient(135deg,#f093fb,#f5576c)',
  'linear-gradient(135deg,#4facfe,#00f2fe)','linear-gradient(135deg,#43e97b,#38f9d7)',
  'linear-gradient(135deg,#fa709a,#fee140)','linear-gradient(135deg,#a18cd1,#fbc2eb)']
function itemColor(skuId: number) { return itemColors[skuId % itemColors.length] }

function formatSpec(s: string): string {
  try { return Object.values(JSON.parse(s)).join(' / ') } catch { return s }
}

function payMethodText(m: number): string {
  const map: Record<number, string> = { 1: '支付宝', 2: '微信支付' }
  return map[m] || '未知'
}

async function load() {
  try {
    const id = route.params.id
    const res: any = await request.get(`/order/${id}`)
    order.value = res.data?.order
    items.value = res.data?.items || []
    logistics.value = res.data?.logistics || null
    payment.value = res.data?.payment || null
  } catch { /* order not found */ }
  finally { loading.value = false }
}

async function payOrder() {
  try {
    await request.post(`/order/${order.value.id}/pay`, { payMethod: 1 })
    ElMessage.success('支付成功')
    load()
  } catch {}
}

async function confirmReceive() {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'warning' })
    await request.post(`/order/${order.value.id}/confirm`)
    ElMessage.success('已确认收货')
    load()
  } catch {}
}

async function cancelOrder() {
  try {
    await ElMessageBox.confirm('确定取消此订单？', '取消订单', { type: 'warning' })
    await request.post(`/order/${order.value.id}/cancel`)
    ElMessage.success('订单已取消')
    load()
  } catch {}
}

onMounted(load)
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f2f5; }
.top-bar { background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.top-inner { display: flex; align-items: center; max-width: 1100px; margin: 0 auto; padding: 12px 20px; gap: 20px; }
.logo { font-size: 22px; font-weight: 700; color: #ff4d4f; text-decoration: none; }
.back-link { color: #999; text-decoration: none; font-size: 14px; }
.back-link:hover { color: #ff4d4f; }
.content-inner { max-width: 960px; margin: 0 auto; padding: 20px; }
.section { margin-bottom: 16px; border-radius: 12px; }
.card-title { font-weight: 700; font-size: 16px; }
.status-bar { display: flex; align-items: center; gap: 16px; }
.status-bar h2 { margin: 0; font-size: 20px; }
.pay-amount { font-size: 20px; font-weight: 700; color: #ff4d4f; }
.item-product { display: flex; align-items: center; gap: 12px; }
.item-img { width: 60px; height: 60px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 24px; color: rgba(255,255,255,0.85); font-weight: bold; flex-shrink: 0; }
.item-name { font-weight: 500; }
.item-spec { color: #999; font-size: 12px; margin-top: 2px; }
.trace-data { margin-top: 12px; }
.trace-title { font-weight: 500; margin-bottom: 6px; }
.trace-data pre { background: #f8f8f8; padding: 12px; border-radius: 8px; font-size: 12px; white-space: pre-wrap; max-height: 200px; overflow-y: auto; }
.action-bar { display: flex; gap: 12px; justify-content: center; padding: 20px 0; }
</style>
