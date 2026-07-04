<template>
  <MerchantLayout>
    <el-card shadow="hover" style="margin-bottom:20px">
        <h3>发送订单返利红包</h3>
        <p style="color:#999">给已完成订单的用户发红包，促进复购</p>
        <el-form :model="form" label-width="100px" style="margin-top:16px">
          <el-form-item label="订单编号"><el-input v-model="form.orderNo" placeholder="输入已完成订单编号，如 SK1783078504724_0"/></el-form-item>
          <el-form-item label="金额(元)"><el-input-number v-model="form.amount" :min="1" :max="1000" :precision="2"/></el-form-item>
          <el-form-item label="祝福语"><el-input v-model="form.message" placeholder="感谢您的购买！"/></el-form-item>
          <el-form-item><el-button type="danger" @click="doSend">发送红包</el-button></el-form-item>
        </el-form>
      </el-card>
  </MerchantLayout>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import MerchantLayout from '@/layouts/MerchantLayout.vue'
import { sendRedEnvelope } from '@/api/redenvelope'
import { ElMessage } from 'element-plus'

const form = ref({ orderNo: '', amount: 20, message: '感谢您的购买！' })
async function doSend() {
  try {
    await sendRedEnvelope({ orderNo: form.value.orderNo, amount: Math.round(form.value.amount * 100), type: 2, message: form.value.message })
    ElMessage.success('红包已发送')
  } catch {}
}
</script>

