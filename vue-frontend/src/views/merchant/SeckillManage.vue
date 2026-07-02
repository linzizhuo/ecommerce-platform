<template>
  <el-container style="min-height:100vh">
    <el-header class="mh"><span>CloudMall 商家后台</span>
      <div><router-link to="/merchant/dashboard">看板</router-link><router-link to="/merchant/products">商品</router-link><router-link to="/merchant/orders">订单</router-link><router-link to="/merchant/coupons">优惠券</router-link><router-link to="/merchant/seckill">秒杀</router-link><router-link to="/merchant/groupbuy">拼团</router-link><router-link to="/merchant/presale">预售</router-link><router-link to="/merchant/distribution">分销</router-link><router-link to="/merchant/combo">套餐</router-link><router-link to="/merchant/redenvelope">红包</router-link><router-link to="/" style="color:#ff4d4f">回前台</router-link></div>
    </el-header>
    <el-main style="max-width:1200px;margin:0 auto;padding:20px">
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
    </el-main>
  </el-container>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
const sessions = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const form = ref({skuId:'',seckillPrice:'',stock:'',startTime:'',endTime:''})
async function load(){loading.value=true;const r:any=await request.get('/seckill/sessions');sessions.value=r.data||[];loading.value=false}
async function save(){await request.post('/merchant/seckill',form.value);ElMessage.success('创建成功');dialog.value=false;load()}
onMounted(load)
</script>
<style scoped>
.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
.mh a{margin:0 10px;color:#666;text-decoration:none;font-size:14px;font-weight:400}
</style>
