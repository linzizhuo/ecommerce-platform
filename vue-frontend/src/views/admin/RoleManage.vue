<template>
  <AdminLayout title="角色权限">
    <el-card style="margin-top:20px"><h3>RBAC 角色列表</h3>
    <el-table :data="roles" style="margin-top:10px">
      <el-table-column prop="id" label="ID" width="60"/><el-table-column prop="roleName" label="角色"/><el-table-column prop="roleCode" label="编码"/><el-table-column prop="description" label="描述"/>
    </el-table>
    </el-card>
  </AdminLayout>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'; import request from '@/utils/request'
import AdminLayout from '@/layouts/AdminLayout.vue'
const roles=ref<any[]>([])
onMounted(async ()=>{ try{ const r:any=await request.get('/admin/roles'); roles.value=r.data||[] }catch{ roles.value=[{id:1,roleName:'超级管理员',roleCode:'ROLE_SUPER_ADMIN',description:'全权限'},{id:2,roleName:'商家',roleCode:'ROLE_MERCHANT',description:'商家权限'},{id:3,roleName:'普通用户',roleCode:'ROLE_USER',description:'普通用户'} ]} })
</script>
