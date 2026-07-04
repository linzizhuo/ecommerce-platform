<template>
  <MerchantLayout>
    <el-card shadow="hover" style="margin-bottom:20px">
        <h3>分销概况</h3>
        <el-row :gutter="20" style="margin-top:16px">
          <el-col :span="12">
            <el-statistic title="分销员总数" :value="settings.totalDistributors || 0" />
          </el-col>
          <el-col :span="12">
            <el-statistic title="累计佣金支出" :value="(settings.totalCommission || 0) / 100" prefix="¥" :precision="2" />
          </el-col>
        </el-row>
      </el-card>
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="userId" label="用户ID"/>
        <el-table-column label="佣金(元)"><template #default="{row}">¥{{(row.commission/100).toFixed(2)}}</template></el-table-column>
        <el-table-column label="可提现(元)"><template #default="{row}">¥{{(row.availableCommission/100).toFixed(2)}}</template></el-table-column>
        <el-table-column prop="level" label="等级"/>
        <el-table-column prop="createTime" label="注册时间" width="170"/>
      </el-table>
  </MerchantLayout>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import MerchantLayout from '@/layouts/MerchantLayout.vue'
import request from '@/utils/request'

const list = ref<any[]>([])
const settings = ref<any>({})

async function load() {
  const r1: any = await request.get('/merchant/distribution/list'); list.value = r1.data || []
  const r2: any = await request.get('/merchant/distribution/settings'); settings.value = r2.data || {}
}
onMounted(load)
</script>

