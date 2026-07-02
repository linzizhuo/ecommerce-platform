<template><div class="page"><el-container><el-aside width="220px" class="aside"><h3>CloudMall</h3><el-menu :default-active="$route.path" background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF" router><el-menu-item index="/admin/dashboard">📊 数据大屏</el-menu-item><el-menu-item index="/admin/statistics">📈 数据统计</el-menu-item><el-menu-item index="/admin/users">👤 用户管理</el-menu-item><el-menu-item index="/admin/merchants">🏪 商家管理</el-menu-item><el-menu-item index="/admin/products">📦 商品审核</el-menu-item><el-menu-item index="/admin/roles">🔐 角色权限</el-menu-item><el-menu-item index="/admin/activities">🎯 活动管理</el-menu-item><el-menu-item index="/admin/dict">📋 数据字典</el-menu-item><el-menu-item index="/admin/violations">⚠️ 违规处罚</el-menu-item><el-menu-item index="/admin/config">⚙️ 系统配置</el-menu-item></el-menu></el-aside><el-main class="main"><h2>角色权限</h2>
<el-card style="margin-top:20px"><h3>RBAC 角色列表</h3>
<el-table :data="roles" style="margin-top:10px">
  <el-table-column prop="id" label="ID" width="60"/><el-table-column prop="roleName" label="角色"/><el-table-column prop="roleCode" label="编码"/><el-table-column prop="description" label="描述"/>
</el-table>
</el-card></el-main></el-container></div></template>
<script setup lang="ts">import { ref, onMounted } from 'vue'; import request from '@/utils/request'
const roles=ref<any[]>([])
onMounted(async ()=>{ try{ const r:any=await request.get('/admin/roles'); roles.value=r.data||[] }catch{ roles.value=[{id:1,roleName:'超级管理员',roleCode:'ROLE_SUPER_ADMIN',description:'全权限'},{id:2,roleName:'商家',roleCode:'ROLE_MERCHANT',description:'商家权限'},{id:3,roleName:'普通用户',roleCode:'ROLE_USER',description:'普通用户'} ]} })
</script>
<style scoped>.page{min-height:100vh;background:#f0f2f5}.aside{background:#304156;min-height:100vh;padding:20px 0}.aside h3{text-align:center;color:#fff;margin-bottom:20px;font-size:20px}.main{background:#f0f2f5;min-height:100vh;padding:24px}</style>
