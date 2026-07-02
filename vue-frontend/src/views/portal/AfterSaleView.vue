<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">售后服务</div>
      <router-link to="/user/center" class="back">返回</router-link>
    </div></header>
    <div class="container">
      <el-button type="danger" @click="dialog=true">申请售后</el-button>
      <el-table :data="list" style="margin-top:16px" v-loading="loading">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="orderId" label="订单号" />
        <el-table-column label="类型"><template #default="{row}">{{ ['','退款','退货退款','换货'][row.type]||'其他' }}</template></el-table-column>
        <el-table-column prop="reason" label="原因" />
        <el-table-column label="状态"><template #default="{row}">{{ ['待审','已通过','已驳回','已完成'][row.status]||'未知' }}</template></el-table-column>
      </el-table>
      <el-dialog v-model="dialog" title="申请售后">
        <el-form :model="form" label-width="80px">
          <el-form-item label="订单号"><el-input v-model="form.orderId" /></el-form-item>
          <el-form-item label="类型"><el-select v-model="form.type"><el-option label="退款" :value="1"/><el-option label="退货退款" :value="2"/><el-option label="换货" :value="3"/></el-select></el-form-item>
          <el-form-item label="原因"><el-input v-model="form.reason" type="textarea" /></el-form-item>
        </el-form>
        <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="danger" @click="submit">提交</el-button></template>
      </el-dialog>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAfterSaleList, applyAfterSale } from '@/api/aftersale'
import { ElMessage } from 'element-plus'
const list = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const form = ref({ orderId: '', type: 1, reason: '' })
async function load() { loading.value=true;const r:any=await getAfterSaleList();list.value=r.data||[];loading.value=false }
async function submit() { await applyAfterSale(form.value); ElMessage.success('提交成功'); dialog.value=false; load() }
onMounted(load)
</script>
<style scoped>
.page{background:#f0f2f5;min-height:100vh}
.top-bar{background:#fff;box-shadow:0 1px 4px rgba(0,0,0,0.06);position:sticky;top:0;z-index:100}
.inner{max-width:1200px;margin:0 auto;padding:0 20px}
.top-row{display:flex;align-items:center;gap:24px;height:60px}
.logo{font-size:22px;font-weight:700;color:#ff4d4f;text-decoration:none}
.title{font-size:20px;font-weight:700;color:#333;flex:1}
.back{color:#999;text-decoration:none;font-size:14px}
.container{max-width:1000px;margin:0 auto;padding:20px}
</style>
