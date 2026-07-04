<template>
  <AdminLayout title="用户管理">
    <el-table :data="users" style="margin-top:15px">
      <el-table-column prop="id" label="ID" width="60"/><el-table-column prop="phone" label="手机号"/><el-table-column prop="nickname" label="昵称"/>
      <el-table-column label="状态" width="100"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'正常':'禁用' }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180"/>
      <el-table-column label="操作" width="120"><template #default="{row}"><el-button size="small" :type="row.status===1?'danger':'success'" @click="toggle(row)">{{ row.status===1?'禁用':'启用' }}</el-button></template></el-table-column>
    </el-table>
  </AdminLayout>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'; import request from '@/utils/request'
import AdminLayout from '@/layouts/AdminLayout.vue'
const users=ref<any[]>([])
async function load(){ const r:any=await request.get('/admin/users'); users.value=r.data||[] }
async function toggle(row:any){ await request.put('/admin/user/'+row.id+'/status',{status:row.status===1?0:1}); load() }
onMounted(load)
</script>
