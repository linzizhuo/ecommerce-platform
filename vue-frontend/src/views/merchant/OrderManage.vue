<template>
  <div class="page">
    <el-container>
      <el-header class="m-header">
        <span class="title">CloudMall 商家后台</span>
        <div><router-link to="/merchant/dashboard">看板</router-link>
          <router-link to="/merchant/products">商品</router-link>
          <router-link to="/merchant/orders">订单</router-link>
          <router-link to="/merchant/coupons">优惠券</router-link></div>
      </el-header>
      <el-main>
        <h3>订单管理</h3>
        <el-table :data="orders">
          <el-table-column prop="orderNo" label="订单号" width="200" />
          <el-table-column label="金额"><template #default="{row}">¥{{ (row.payAmount/100).toFixed(2) }}</template></el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{row}">
              <el-tag v-if="row.status===1" type="warning">待发货</el-tag>
              <el-tag v-else-if="row.status===2" type="primary">已发货</el-tag>
              <el-tag v-else>{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250">
            <template #default="{row}">
              <el-button v-if="row.status===1" type="primary" size="small" @click="shipOrder(row)">发货</el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 发货弹窗 -->
        <el-dialog v-model="shipVisible" title="发货" width="400px">
          <el-input v-model="company" placeholder="快递公司" style="margin-bottom:10px" />
          <el-input v-model="trackingNo" placeholder="快递单号" />
          <template #footer><el-button @click="shipVisible=false">取消</el-button><el-button type="primary" @click="doShip">确认发货</el-button></template>
        </el-dialog>
      </el-main>
    </el-container>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
const orders = ref<any[]>([])
const shipVisible = ref(false)
const shipId = ref(0)
const company = ref('顺丰快递')
const trackingNo = ref('SF' + Date.now())
async function loadOrders() { const res: any = await request.get('/product/merchant/orders'); orders.value = res.data || [] }
function shipOrder(row: any) { shipId.value = row.id; shipVisible.value = true }
async function doShip() {
  await request.post(`/order/${shipId.value}/ship`, { company: company.value, trackingNo: trackingNo.value })
  ElMessage.success('发货成功'); shipVisible.value = false; loadOrders()
}
onMounted(loadOrders)
</script>
<style scoped>.page { min-height: 100vh; background: #f5f5f5; }
.m-header { background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.08); display: flex; align-items: center; justify-content: space-between; }
.title { font-size: 20px; font-weight: bold; color: #1890ff; }
.m-header a { margin: 0 10px; color: #666; text-decoration: none; }
.el-main { max-width: 1200px; margin: 20px auto; }</style>
