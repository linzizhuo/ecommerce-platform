<template><div class="page"><el-container><el-aside width="220px" class="aside"><h3>CloudMall</h3><el-menu :default-active="$route.path" background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF" router><el-menu-item index="/admin/dashboard">📊 数据大屏</el-menu-item><el-menu-item index="/admin/users">👤 用户管理</el-menu-item><el-menu-item index="/admin/merchants">🏪 商家管理</el-menu-item><el-menu-item index="/admin/products">📦 商品审核</el-menu-item><el-menu-item index="/admin/roles">🔐 角色权限</el-menu-item></el-menu></el-aside><el-main class="main"><h2>用户管理</h2>
<el-table :data="users" style="margin-top:15px">
  <el-table-column prop="id" label="ID" width="60"/><el-table-column prop="phone" label="手机号"/><el-table-column prop="nickname" label="昵称"/>
  <el-table-column label="状态" width="100"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'正常':'禁用' }}</el-tag></template></el-table-column>
  <el-table-column prop="createTime" label="注册时间" width="180"/>
  <el-table-column label="操作" width="120"><template #default="{row}"><el-button size="small" :type="row.status===1?'danger':'success'" @click="toggle(row)">{{ row.status===1?'禁用':'启用' }}</el-button></template></el-table-column>
</el-table></el-main></el-container></div></template>
<script setup lang="ts">import { ref, onMounted } from 'vue'; import request from '@/utils/request'
const users=ref<any[]>([])
async function load(){ const r:any=await request.get('/admin/users'); users.value=r.data||[] }
async function toggle(row:any){ await request.put('/admin/user/'+row.id+'/status',{status:row.status===1?0:1}); load() }
onMounted(load)</script>
<style scoped>.page{min-height:100vh;background:#f0f2f5}.aside{background:#304156;min-height:100vh;padding:20px 0}.aside h3{text-align:center;color:#fff;margin-bottom:20px;font-size:20px}.main{background:#f0f2f5;min-height:100vh;padding:24px}</style>
