<template>
  <AdminLayout title="⚠️ 违规处罚管理">
    <el-button type="danger" @click="dialog=true" style="margin-bottom:16px">新增处罚</el-button>
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="merchantId" label="商家ID"/>
      <el-table-column label="违规类型" width="120">
        <template #default="{row}">{{ ['','商品违规','虚假发货','欺诈'][row.type] || '未知' }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" width="200"/>
      <el-table-column label="处罚方式" width="120">
        <template #default="{row}">{{ ['','警告','罚款','下架商品','封店'][row.penaltyType] || '未知' }}</template>
      </el-table-column>
      <el-table-column label="罚金(元)" width="100">
        <template #default="{row}">¥{{ (row.penaltyAmount/100).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status===1?'success':row.status===2?'info':'warning'">
            {{ ['待执行','已执行','已申诉'][row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="170"/>
      <el-table-column label="操作" width="120">
        <template #default="{row}">
          <el-button v-if="row.status===0" size="small" type="success" @click="exec(row.id,1)">执行</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialog" title="新增违规处罚" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="商家ID"><el-input v-model="form.merchantId"/></el-form-item>
        <el-form-item label="违规类型">
          <el-select v-model="form.type">
            <el-option label="商品违规" :value="1"/>
            <el-option label="虚假发货" :value="2"/>
            <el-option label="欺诈" :value="3"/>
          </el-select>
        </el-form-item>
        <el-form-item label="处罚方式">
          <el-select v-model="form.penaltyType">
            <el-option label="警告" :value="1"/>
            <el-option label="罚款" :value="2"/>
            <el-option label="下架商品" :value="3"/>
            <el-option label="封店" :value="4"/>
          </el-select>
        </el-form-item>
        <el-form-item label="罚金(元)"><el-input-number v-model="form.penaltyAmount" :min="0"/></el-form-item>
        <el-form-item label="原因"><el-input v-model="form.reason" type="textarea"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="danger" @click="save">保存</el-button></template>
    </el-dialog>
  </AdminLayout>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import AdminLayout from '@/layouts/AdminLayout.vue'

const list = ref<any[]>([])
const dialog = ref(false)
const form = ref({ merchantId:'', type:1, penaltyType:1, penaltyAmount:0, reason:'' })

async function load() { const r: any = await request.get('/admin/violations'); list.value = r.data || [] }
async function save() {
  await request.post('/admin/violation', form.value)
  ElMessage.success('已创建'); dialog.value = false; load()
}
async function exec(id: number, status: number) {
  await request.put('/admin/violation/' + id, { status })
  ElMessage.success('已执行'); load()
}
onMounted(load)
</script>
