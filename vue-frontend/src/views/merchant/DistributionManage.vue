<template>
  <el-container style="min-height:100vh">
    <el-header class="mh"><span>CloudMall 商家后台</span>
      <div><router-link to="/merchant/dashboard">看板</router-link><router-link to="/merchant/products">商品</router-link><router-link to="/merchant/orders">订单</router-link><router-link to="/merchant/coupons">优惠券</router-link><router-link to="/merchant/seckill">秒杀</router-link><router-link to="/merchant/groupbuy">拼团</router-link><router-link to="/merchant/presale">预售</router-link><router-link to="/merchant/distribution">分销</router-link><router-link to="/merchant/combo">套餐</router-link><router-link to="/merchant/redenvelope">红包</router-link><router-link to="/" style="color:#ff4d4f">回前台</router-link></div>
    </el-header>
    <el-main style="max-width:1200px;margin:0 auto;padding:20px">
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
    </el-main>
  </el-container>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const list = ref<any[]>([])
const settings = ref<any>({})

async function load() {
  const r1: any = await request.get('/merchant/distribution/list'); list.value = r1.data || []
  const r2: any = await request.get('/merchant/distribution/settings'); settings.value = r2.data || {}
}
onMounted(load)
</script>
<style scoped>
.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
.mh a{margin:0 10px;color:#666;text-decoration:none;font-size:14px;font-weight:400}
</style>
