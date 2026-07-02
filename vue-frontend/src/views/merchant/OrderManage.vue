<template>
  <div class="page">
    <el-container>
      <el-header class="mh"><span>CloudMall 商家后台</span>
        <div><router-link to="/merchant/dashboard">看板</router-link>
          <router-link to="/merchant/products">商品</router-link>
          <router-link to="/merchant/orders">订单</router-link>
          <router-link to="/merchant/coupons">优惠券</router-link>
          <router-link to="/merchant/seckill">秒杀</router-link>
          <router-link to="/merchant/groupbuy">拼团</router-link>
          <router-link to="/merchant/presale">预售</router-link>
          <router-link to="/merchant/distribution">分销</router-link>
          <router-link to="/merchant/combo">套餐</router-link>
          <router-link to="/merchant/redenvelope">红包</router-link>
          <router-link to="/merchant/reconciliation">对账</router-link>
          <router-link to="/merchant/stock-report">库存</router-link>
          <router-link to="/" style="color:#ff4d4f">回前台</router-link></div>
      </el-header>
      <el-main>
        <el-tabs v-model="activeTab">
          <!-- 订单管理 -->
          <el-tab-pane label="📋 订单管理" name="orders">
            <el-row :gutter="10" style="margin-bottom:12px">
              <el-button :type="filterStatus===null?'primary':''" @click="filterStatus=null;loadOrders()">全部</el-button>
              <el-button :type="filterStatus===0?'primary':''" @click="filterStatus=0;loadOrders()">待审核</el-button>
              <el-button :type="filterStatus===1?'primary':''" @click="filterStatus=1;loadOrders()">待发货</el-button>
              <el-button :type="filterStatus===2?'primary':''" @click="filterStatus=2;loadOrders()">已发货</el-button>
              <el-button :type="filterStatus===3?'primary':''" @click="filterStatus=3;loadOrders()">已完成</el-button>
            </el-row>
            <el-table :data="filteredOrders">
              <el-table-column prop="orderNo" label="订单号" width="200" />
              <el-table-column label="金额"><template #default="{row}">¥{{ (row.payAmount/100).toFixed(2) }}</template></el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="{row}">
                  <el-tag v-if="row.status===0" type="info">待审核</el-tag>
                  <el-tag v-else-if="row.status===1" type="warning">待发货</el-tag>
                  <el-tag v-else-if="row.status===2" type="primary">已发货</el-tag>
                  <el-tag v-else-if="row.status===3" type="success">已完成</el-tag>
                  <el-tag v-else>{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="280">
                <template #default="{row}">
                  <el-button v-if="row.status===0" type="success" size="small" @click="auditOrder(row)">审核通过</el-button>
                  <el-button v-if="row.status===1" type="primary" size="small" @click="shipOrder(row)">发货</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <!-- 售后处理 -->
          <el-tab-pane label="🔄 售后处理" name="aftersale">
            <el-table :data="aftersales">
              <el-table-column prop="id" label="ID" width="80"/>
              <el-table-column prop="orderId" label="关联订单" width="100"/>
              <el-table-column label="类型" width="100">
                <template #default="{row}">{{ ['','退款','退货退款','换货'][row.type]||'其他' }}</template>
              </el-table-column>
              <el-table-column prop="reason" label="原因"/>
              <el-table-column label="金额" width="100"><template #default="{row}">¥{{ ((row.amount||0)/100).toFixed(2) }}</template></el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{row}">{{ ['待处理','处理中','已完成','已拒绝'][row.status]||'待处理' }}</template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{row}">
                  <el-button v-if="row.status===0" type="success" size="small" @click="handleAfter(row.id,1)">同意</el-button>
                  <el-button v-if="row.status===0" type="danger" size="small" @click="handleAfter(row.id,3)">拒绝</el-button>
                  <el-button v-if="row.status===1" type="primary" size="small" @click="handleAfter(row.id,2)">完成</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('orders')
const orders = ref<any[]>([])
const aftersales = ref<any[]>([])
const filterStatus = ref<number | null>(null)
const shipVisible = ref(false)
const shipId = ref(0)
const company = ref('顺丰快递')
const trackingNo = ref('SF' + Date.now())

const filteredOrders = computed(() => {
  if (filterStatus.value === null) return orders.value
  return orders.value.filter(o => o.status === filterStatus.value)
})

async function loadOrders() { const res: any = await request.get('/product/merchant/orders'); orders.value = res.data || [] }
async function loadAftersales() { const res: any = await request.get('/merchant/aftersales'); aftersales.value = res.data || [] }

async function auditOrder(row: any) {
  await request.put('/order/' + row.id, { status: 1, remark: '商家审核通过' })
  ElMessage.success('审核通过，订单进入待发货'); loadOrders()
}
function shipOrder(row: any) { shipId.value = row.id; shipVisible.value = true }
async function doShip() {
  await request.post(`/order/${shipId.value}/ship`, { company: company.value, trackingNo: trackingNo.value })
  ElMessage.success('发货成功'); shipVisible.value = false; loadOrders()
}
async function handleAfter(id: number, status: number) {
  await request.put('/merchant/aftersale/' + id, { status })
  ElMessage.success('处理成功'); loadAftersales()
}
onMounted(() => { loadOrders(); loadAftersales() })
</script>
<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }
.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
.mh a{margin:0 6px;color:#666;text-decoration:none;font-size:13px;font-weight:400}
.mh a.router-link-active{color:#1890ff}
.el-main { max-width: 1200px; margin: 20px auto; }
</style>
