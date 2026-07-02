<template>
  <el-container style="min-height:100vh">
    <el-header class="mh"><span>CloudMall 商家后台</span>
      <div><router-link to="/merchant/dashboard">看板</router-link><router-link to="/merchant/products">商品</router-link><router-link to="/merchant/orders">订单</router-link><router-link to="/merchant/coupons">优惠券</router-link><router-link to="/merchant/seckill">秒杀</router-link><router-link to="/merchant/groupbuy">拼团</router-link><router-link to="/merchant/presale">预售</router-link><router-link to="/merchant/distribution">分销</router-link><router-link to="/merchant/combo">套餐</router-link><router-link to="/merchant/redenvelope">红包</router-link><router-link to="/" style="color:#ff4d4f">回前台</router-link></div>
    </el-header>
    <el-main style="max-width:1200px;margin:0 auto;padding:20px">
      <el-table :data="groups" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="skuId" label="SKU"/>
        <el-table-column label="拼团价"><template #default="{row}">¥{{((row.groupPrice||0)/100).toFixed(2)}}</template></el-table-column>
        <el-table-column label="进度"><template #default="{row}">{{row.currentCount}}/{{row.requiredCount}}</template></el-table-column>
        <el-table-column label="状态"><template #default="{row}">{{['拼团中','已成团','已失败'][row.status]||'未知'}}</template></el-table-column>
        <el-table-column label="操作"><template #default="{row}"><el-button size="small" @click="$router.push(`/groupbuy/${row.id}`)">查看</el-button></template></el-table-column>
      </el-table>
    </el-main>
  </el-container>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
const groups = ref<any[]>([])
const loading = ref(false)
async function load(){loading.value=true;const r:any=await request.get('/merchant/groupbuy/list');groups.value=r.data||[];loading.value=false}
onMounted(load)
</script>
<style scoped>
.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
.mh a{margin:0 10px;color:#666;text-decoration:none;font-size:14px;font-weight:400}
</style>
