<template>
  <div class="page">
    <MerchantLayout>
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
    </MerchantLayout>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import MerchantLayout from '@/layouts/MerchantLayout.vue'
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
.page { min-height: 100vh; background: #f5f5f5; }
.mh a.router-link-active{color:#1890ff}
.el-main { max-width: 1200px; margin: 20px auto; }
