<template>
  <MerchantLayout>
    <el-button type="danger" @click="dialog=true" style="margin-bottom:16px">创建秒杀场次</el-button>
      <el-table :data="sessions" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="skuId" label="SKU"/>
        <el-table-column label="秒杀价"><template #default="{row}">¥{{(row.seckillPrice/100).toFixed(2)}}</template></el-table-column>
        <el-table-column prop="stock" label="库存"/>
        <el-table-column prop="startTime" label="开始时间"/>
        <el-table-column label="状态"><template #default="{row}">{{['未开始','进行中','已结束'][row.status]||'未知'}}</template></el-table-column>
      </el-table>
      <el-dialog v-model="dialog" title="创建秒杀场次">
        <el-form :model="form" label-width="80px">
          <el-form-item label="SKU"><el-input v-model="form.skuId"/></el-form-item>
          <el-form-item label="秒杀价"><el-input v-model="form.seckillPrice"/></el-form-item>
          <el-form-item label="库存"><el-input v-model="form.stock"/></el-form-item>
          <el-form-item label="开始时间"><el-input v-model="form.startTime" placeholder="2026-01-01 00:00:00"/></el-form-item>
          <el-form-item label="结束时间"><el-input v-model="form.endTime"/></el-form-item>
        </el-form>
        <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="danger" @click="save">保存</el-button></template>
      </el-dialog>
</MerchantLayout>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import MerchantLayout from '@/layouts/MerchantLayout.vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
const sessions = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const form = ref({skuId:'',seckillPrice:'',stock:'',startTime:'',endTime:''})
async function load(){loading.value=true;const r:any=await request.get('/merchant/seckill/list');sessions.value=r.data||[];loading.value=false}
async function save(){await request.post('/merchant/seckill',form.value);ElMessage.success('创建成功');dialog.value=false;load()}
onMounted(load)
</script>

