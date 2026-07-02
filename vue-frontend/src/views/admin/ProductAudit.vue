<template><div class="page"><el-container><el-aside width="220px" class="aside"><h3>CloudMall</h3><el-menu :default-active="$route.path" background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF" router><el-menu-item index="/admin/dashboard">📊 数据大屏</el-menu-item><el-menu-item index="/admin/statistics">📈 数据统计</el-menu-item><el-menu-item index="/admin/users">👤 用户管理</el-menu-item><el-menu-item index="/admin/merchants">🏪 商家管理</el-menu-item><el-menu-item index="/admin/products">📦 商品审核</el-menu-item><el-menu-item index="/admin/roles">🔐 角色权限</el-menu-item><el-menu-item index="/admin/activities">🎯 活动管理</el-menu-item><el-menu-item index="/admin/dict">📋 数据字典</el-menu-item><el-menu-item index="/admin/violations">⚠️ 违规处罚</el-menu-item><el-menu-item index="/admin/config">⚙️ 系统配置</el-menu-item></el-menu></el-aside><el-main class="main"><h2>商品审核</h2>
<el-table :data="products" style="margin-top:15px">
  <el-table-column prop="id" label="ID" width="60"/><el-table-column prop="name" label="商品"/><el-table-column prop="brand" label="品牌" width="100"/>
  <el-table-column label="状态" width="100"><template #default="{row}"><el-tag v-if="row.status===2" type="success">已上架</el-tag><el-tag v-else-if="row.status===1" type="warning">待审核</el-tag><el-tag v-else type="info">草稿</el-tag></template></el-table-column>
  <el-table-column label="操作" width="200"><template #default="{row}"><el-button v-if="row.status===1" type="success" size="small" @click="audit(row,2)">通过</el-button><el-button v-if="row.status===1" type="danger" size="small" @click="audit(row,3)">驳回</el-button><el-button v-if="row.status===2" type="warning" size="small" @click="audit(row,3)">下架</el-button></template></el-table-column>
</el-table></el-main></el-container></div></template>
<script setup lang="ts">import { ref, onMounted } from 'vue'; import request from '@/utils/request'
const products=ref<any[]>([])
async function load(){ const r:any=await request.get('/admin/products'); products.value=r.data||[] }
async function audit(row:any,status:number){ await request.put('/admin/product/'+row.id+'/status',{status}); load() }
onMounted(load)
</script>
<style scoped>.page{min-height:100vh;background:#f0f2f5}.aside{background:#304156;min-height:100vh;padding:20px 0}.aside h3{text-align:center;color:#fff;margin-bottom:20px;font-size:20px}.main{background:#f0f2f5;min-height:100vh;padding:24px}</style>
