<template>
  <div class="page">
    <el-container>
      <el-header class="mh"><span>CloudMall 商家后台</span>
        <div>
          <router-link to="/merchant/dashboard">看板</router-link>
          <router-link to="/merchant/products">商品</router-link>
          <router-link to="/merchant/orders">订单</router-link>
          <router-link to="/merchant/coupons">优惠券</router-link>
          <router-link to="/merchant/seckill">秒杀</router-link>
          <router-link to="/merchant/groupbuy">拼团</router-link>
          <router-link to="/merchant/presale">预售</router-link>
          <router-link to="/merchant/distribution">分销</router-link>
          <router-link to="/merchant/combo">套餐</router-link>
          <router-link to="/merchant/redenvelope">红包</router-link>
          <router-link to="/merchant/activities">活动</router-link>
          <router-link to="/merchant/reconciliation">对账</router-link>
          <router-link to="/merchant/stock-report">库存</router-link>
          <router-link to="/" style="color:#ff4d4f">回前台</router-link>
        </div>
      </el-header>
      <el-main>
        <h3>💰 对账报表</h3>
        <el-row :gutter="12" style="margin-bottom:16px">
          <el-col :span="6"><el-date-picker v-model="startDate" type="date" placeholder="开始日期" style="width:100%" /></el-col>
          <el-col :span="6"><el-date-picker v-model="endDate" type="date" placeholder="结束日期" style="width:100%" /></el-col>
          <el-col :span="6"><el-button type="primary" @click="load">查询</el-button></el-col>
        </el-row>
        <el-table :data="list" stripe>
          <el-table-column prop="date" label="日期" width="140" />
          <el-table-column prop="orderCount" label="订单数" />
          <el-table-column label="总金额">
            <template #default="{row}">¥{{ fmtMoney(row.totalAmount) }}</template>
          </el-table-column>
        </el-table>
        <div style="margin-top:20px;text-align:right;font-size:18px">
          合计: <b>{{ totalOrders }}</b> 单，金额 <b style="color:#ff4d4f">¥{{ fmtMoney(totalAmount) }}</b>
        </div>
      </el-main>
    </el-container>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import request from '@/utils/request'

const list = ref<any[]>([])
const startDate = ref('')
const endDate = ref('')

function fmtMoney(v: any) { return ((Number(v) || 0) / 100).toFixed(2) }

const totalOrders = computed(() => list.value.reduce((s, r) => s + (r.orderCount || 0), 0))
const totalAmount = computed(() => list.value.reduce((s, r) => s + (r.totalAmount || 0), 0))

async function load() {
  const params: any = {}
  if (startDate.value) params.startDate = startDate.value
  if (endDate.value) params.endDate = endDate.value
  const r: any = await request.get('/merchant/reconciliation', { params })
  list.value = r.data || []
}
load()
</script>
<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }
.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
.mh a{margin:0 8px;color:#666;text-decoration:none;font-size:13px;font-weight:400}
.mh a.router-link-active{color:#1890ff}
.el-main { max-width: 1200px; margin: 20px auto; }
</style>
