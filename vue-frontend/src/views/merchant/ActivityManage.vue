<template>
  <div class="page">
    <MerchantLayout>
    <h3>🎯 限时活动管理</h3>
        <el-button type="primary" @click="dialog=true" style="margin-bottom:16px">创建活动</el-button>
        <el-table :data="list" stripe>
          <el-table-column prop="id" label="ID" width="80"/>
          <el-table-column prop="name" label="活动名称"/>
          <el-table-column label="类型" width="100">
            <template #default="{row}">{{ typeMap[row.type] || '未知' }}</template>
          </el-table-column>
          <el-table-column prop="rules" label="规则" width="180"/>
          <el-table-column prop="startTime" label="开始时间" width="170"/>
          <el-table-column prop="endTime" label="结束时间" width="170"/>
          <el-table-column label="状态" width="100">
            <template #default="{row}">{{ ['草稿','待审核','进行中','已结束'][row.status] || '未知' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{row}">
              <el-button v-if="row.status===1 || row.status===2" size="small" type="warning" @click="endActivity(row.id)">结束</el-button>
              <el-button size="small" type="danger" @click="delActivity(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-dialog v-model="dialog" title="创建活动" width="500px">
          <el-form :model="form" label-width="100px">
            <el-form-item label="活动名称"><el-input v-model="form.name"/></el-form-item>
            <el-form-item label="类型">
              <el-select v-model="form.type">
                <el-option label="限时折扣" :value="1"/>
                <el-option label="满减" :value="5"/>
              </el-select>
            </el-form-item>
            <el-form-item label="规则JSON"><el-input v-model="form.rules" type="textarea" placeholder='{"threshold":20000,"reduce":3000}'/></el-form-item>
            <el-form-item label="开始时间"><el-input v-model="form.startTime" placeholder="2026-07-01 00:00:00"/></el-form-item>
            <el-form-item label="结束时间"><el-input v-model="form.endTime"/></el-form-item>
          </el-form>
          <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
        </el-dialog>
    </MerchantLayout>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import MerchantLayout from '@/layouts/MerchantLayout.vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref<any[]>([])
const dialog = ref(false)
const form = ref({ name:'', type:1, rules:'', startTime:'', endTime:'' })
const typeMap: any = { 1:'限时折扣', 2:'拼团', 3:'秒杀', 4:'预售', 5:'满减' }

async function load() { const r: any = await request.get('/merchant/activities'); list.value = r.data || [] }
async function save() { await request.post('/merchant/activity', form.value); ElMessage.success('创建成功'); dialog.value = false; load() }
async function endActivity(id: number) { await request.put('/merchant/activity/' + id, { status: 3 }); ElMessage.success('已结束'); load() }
async function delActivity(id: number) { await request.delete('/merchant/activity/' + id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
.page { min-height: 100vh; background: #f5f5f5; }
.mh a.router-link-active{color:#1890ff}
.el-main { max-width: 1200px; margin: 20px auto; }
