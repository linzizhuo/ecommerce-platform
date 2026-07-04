<template>
  <AdminLayout title="商品审核">
    <el-table :data="products" style="margin-top:15px">
      <el-table-column prop="id" label="ID" width="60"/><el-table-column prop="name" label="商品"/><el-table-column prop="brand" label="品牌" width="100"/>
      <el-table-column label="状态" width="100"><template #default="{row}"><el-tag v-if="row.status===2" type="success">已上架</el-tag><el-tag v-else-if="row.status===1" type="warning">待审核</el-tag><el-tag v-else type="info">草稿</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200"><template #default="{row}"><el-button v-if="row.status===1" type="success" size="small" @click="audit(row,2)">通过</el-button><el-button v-if="row.status===1" type="danger" size="small" @click="audit(row,3)">驳回</el-button><el-button v-if="row.status===2" type="warning" size="small" @click="audit(row,3)">下架</el-button></template></el-table-column>
    </el-table>
  </AdminLayout>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'; import request from '@/utils/request'
import AdminLayout from '@/layouts/AdminLayout.vue'
const products=ref<any[]>([])
async function load(){ const r:any=await request.get('/admin/products'); products.value=r.data||[] }
async function audit(row:any,status:number){ await request.put('/admin/product/'+row.id+'/status',{status}); load() }
onMounted(load)
</script>
