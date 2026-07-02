<template>
  <div class="page">
    <header class="top-bar"><div class="top-inner"><router-link to="/" class="logo">CloudMall</router-link></div></header>
    <div class="content-inner">
      <h2>我的订单</h2>
      <el-radio-group v-model="statusFilter" @change="loadOrders" style="margin-bottom:20px">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">待付款</el-radio-button>
        <el-radio-button :value="1">待发货</el-radio-button>
        <el-radio-button :value="2">待收货</el-radio-button>
        <el-radio-button :value="3">已完成</el-radio-button>
      </el-radio-group>
      <el-table :data="orders" style="width:100%">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column label="金额"><template #default="{row}">¥{{ (row.payAmount/100).toFixed(2) }}</template></el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag v-if="row.status===0" type="warning">待付款</el-tag>
            <el-tag v-else-if="row.status===1">待发货</el-tag>
            <el-tag v-else-if="row.status===2" type="primary">待收货</el-tag>
            <el-tag v-else-if="row.status===3" type="success">已完成</el-tag>
            <el-tag v-else type="info">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column label="操作" width="280">
          <template #default="{row}">
            <el-button size="small" @click="$router.push(`/order/${row.id}`)">查看详情</el-button>
            <el-button v-if="row.status===0" type="danger" size="small" @click="payOrder(row)">去支付</el-button>
            <el-button v-if="row.status===2" type="primary" size="small" @click="confirmReceive(row)">确认收货</el-button>
            <el-button v-if="row.status===3" type="warning" size="small" @click="openReview(row)">⭐ 评价</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 评价弹窗 -->
      <el-dialog v-model="reviewVisible" title="商品评价" width="450px">
        <div style="text-align:center">
          <p style="margin-bottom:15px;font-size:16px">订单号: {{ reviewOrder?.orderNo }}</p>
          <el-rate v-model="reviewRating" show-text :texts="['很差','较差','一般','较好','非常好']" style="justify-content:center" />
          <el-input v-model="reviewContent" type="textarea" placeholder="分享你的使用感受..." rows="3" style="margin-top:15px" />
        </div>
        <template #footer>
          <el-button @click="reviewVisible=false">取消</el-button>
          <el-button type="danger" @click="submitReview">提交评价</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const orders = ref<any[]>([]); const statusFilter = ref<number|null>(null)
const reviewVisible = ref(false); const reviewRating = ref(5)
const reviewContent = ref(''); const reviewOrder = ref<any>(null)

async function loadOrders() {
  const res: any = await request.get('/order', { params: { status: statusFilter.value } })
  orders.value = res.data || []
}
async function payOrder(row: any) {
  await request.post(`/order/${row.id}/pay`, { payMethod: 1 }); ElMessage.success('支付成功'); loadOrders()
}
async function confirmReceive(row: any) {
  await request.post(`/order/${row.id}/confirm`); ElMessage.success('已确认收货'); loadOrders()
}
function openReview(row: any) {
  reviewOrder.value = row
  reviewRating.value = 5
  reviewContent.value = ''
  reviewVisible.value = true
}
async function submitReview() {
  // 获取订单商品
  const res: any = await request.get(`/order/${reviewOrder.value.id}`)
  const items = res.data?.items || []
  // 为每个商品提交评价
  for (const item of items) {
    await request.post('/review', { productId: item.skuId, rating: reviewRating.value, content: reviewContent.value })
  }
  ElMessage.success('评价成功!')
  reviewVisible.value = false
}

onMounted(loadOrders)
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f2f5; }
.top-bar { background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.top-inner { display: flex; align-items: center; max-width: 1100px; margin: 0 auto; padding: 12px 20px; }
.content-inner { max-width: 1100px; margin: 0 auto; padding: 20px; }
.logo { font-size: 22px; font-weight: 700; color: #ff4d4f; text-decoration: none; }
</style>
