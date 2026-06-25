<template>
  <div class="page">
    <header class="top-bar"><div class="top-inner"><router-link to="/" class="logo">CloudMall</router-link>
      <router-link to="/cart"><el-icon><ShoppingCart /></el-icon> 购物车</router-link>
      <router-link to="/orders">我的订单</router-link></div></header>
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
        <el-table-column label="状态" width="120">
          <template #default="{row}">
            <el-tag v-if="row.status===0" type="warning">待付款</el-tag>
            <el-tag v-else-if="row.status===1">待发货</el-tag>
            <el-tag v-else-if="row.status===2" type="primary">待收货</el-tag>
            <el-tag v-else-if="row.status===3" type="success">已完成</el-tag>
            <el-tag v-else type="info">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{row}">
            <el-button v-if="row.status===0" type="danger" size="small" @click="payOrder(row)">去支付</el-button>
            <el-button v-if="row.status===2" type="primary" size="small" @click="confirmReceive(row)">确认收货</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
const orders = ref<any[]>([])
const statusFilter = ref<number | null>(null)
async function loadOrders() {
  const res: any = await request.get('/order', { params: { status: statusFilter.value } })
  orders.value = res.data || []
}
async function payOrder(row: any) {
  await request.post(`/order/${row.id}/pay`, { payMethod: 1 })
  ElMessage.success('支付成功')
  loadOrders()
}
async function confirmReceive(row: any) {
  await request.post(`/order/${row.id}/confirm`)
  ElMessage.success('已确认收货')
  loadOrders()
}
onMounted(loadOrders)
</script>
<style scoped>.page { min-height: 100vh; background: #f5f5f5; }
.top-bar { background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.top-inner { display: flex; align-items: center; max-width: 1100px; margin: 0 auto; padding: 12px 20px; }
.content-inner { max-width: 1100px; margin: 0 auto; padding: 20px; }
.logo { font-size: 24px; font-weight: bold; color: #ff4d4f; text-decoration: none; }</style>
