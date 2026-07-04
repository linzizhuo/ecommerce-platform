<template>
  <MerchantLayout>
    <el-table :data="groups" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="skuId" label="SKU"/>
      <el-table-column label="拼团价"><template #default="{row}">¥{{((row.groupPrice||0)/100).toFixed(2)}}</template></el-table-column>
      <el-table-column label="进度"><template #default="{row}">{{row.currentCount}}/{{row.requiredCount}}</template></el-table-column>
      <el-table-column label="状态"><template #default="{row}">{{['拼团中','已成团','已失败'][row.status]||'未知'}}</template></el-table-column>
      <el-table-column label="操作"><template #default="{row}"><el-button size="small" @click="$router.push(`/groupbuy/${row.id}`)">查看</el-button></template></el-table-column>
    </el-table>
  </MerchantLayout>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import MerchantLayout from '@/layouts/MerchantLayout.vue'
const groups = ref<any[]>([])
const loading = ref(false)
async function load(){loading.value=true;const r:any=await request.get('/merchant/groupbuy/list');groups.value=r.data||[];loading.value=false}
onMounted(load)
</script>
