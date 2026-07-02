<template>
  <el-container style="min-height:100vh">
    <el-aside width="200px" style="background:#304156">
      <div style="color:#fff;text-align:center;padding:20px 0;font-size:18px;font-weight:700">运营后台</div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#ff4d4f">
        <el-menu-item index="/admin/dashboard">📊 数据看板</el-menu-item>
        <el-menu-item index="/admin/statistics">📈 数据统计</el-menu-item>
        <el-menu-item index="/admin/users">👤 用户管理</el-menu-item>
        <el-menu-item index="/admin/merchants">🏪 商家管理</el-menu-item>
        <el-menu-item index="/admin/products">📦 商品审核</el-menu-item>
        <el-menu-item index="/admin/activities">🎯 活动管理</el-menu-item>
        <el-menu-item index="/admin/roles">🔐 角色权限</el-menu-item>
        <el-menu-item index="/admin/dict">📋 数据字典</el-menu-item>
        <el-menu-item index="/admin/violations">⚠️ 违规处罚</el-menu-item>
        <el-menu-item index="/admin/config">⚙️ 系统配置</el-menu-item>
      </el-menu>
    </el-aside>
    <el-main style="background:#f0f2f5;padding:20px">
      <h2 style="margin-bottom:16px">🎯 活动管理</h2>
      <el-row :gutter="12" style="margin-bottom:16px">
        <el-col :span="3" v-for="t in typeFilters" :key="t.value">
          <el-button :type="filterType===t.value?'primary':''" @click="filterType=t.value;load()" size="small">{{ t.label }}</el-button>
        </el-col>
      </el-row>
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
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button v-if="row.status===1" size="small" type="success" @click="audit(row.id,2)">通过</el-button>
            <el-button v-if="row.status===2" size="small" type="warning" @click="audit(row.id,3)">结束</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-dialog v-model="dialog" title="创建活动" width="500px">
        <el-form :model="form" label-width="100px">
          <el-form-item label="活动名称"><el-input v-model="form.name"/></el-form-item>
          <el-form-item label="类型">
            <el-select v-model="form.type">
              <el-option v-for="t in typeFilters.filter(x=>x.value)" :key="t.value" :label="t.label" :value="t.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="规则JSON"><el-input v-model="form.rules" type="textarea" placeholder='{"threshold":20000,"reduce":3000}'/></el-form-item>
          <el-form-item label="开始时间"><el-input v-model="form.startTime" placeholder="2026-06-01 00:00:00"/></el-form-item>
          <el-form-item label="结束时间"><el-input v-model="form.endTime"/></el-form-item>
        </el-form>
        <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
      </el-dialog>
    </el-main>
  </el-container>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref<any[]>([])
const dialog = ref(false)
const filterType = ref<number | null>(null)
const form = ref({ name:'', type:5, rules:'', startTime:'', endTime:'' })
const typeFilters = [
  { label:'全部', value: null as any },
  { label:'限时折扣', value:1 }, { label:'拼团', value:2 }, { label:'秒杀', value:3 },
  { label:'预售', value:4 }, { label:'满减', value:5 }
]
const typeMap: any = { 1:'限时折扣', 2:'拼团', 3:'秒杀', 4:'预售', 5:'满减' }

async function load() {
  const r: any = await request.get('/admin/activities', { params: { type: filterType.value } })
  list.value = r.data || []
}
async function save() {
  await request.post('/admin/activity', form.value)
  ElMessage.success('创建成功'); dialog.value = false; load()
}
async function audit(id: number, status: number) {
  await request.put('/admin/activity/' + id + '/status', { status })
  ElMessage.success('操作成功'); load()
}
onMounted(load)
</script>
