<template>
  <el-container style="min-height:100vh">
    <el-aside width="200px" style="background:#304156">
      <div style="color:#fff;text-align:center;padding:20px 0;font-size:18px;font-weight:700">运营后台</div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/admin/dashboard">📊 数据看板</el-menu-item>
        <el-menu-item index="/admin/statistics">📈 数据统计</el-menu-item>
        <el-menu-item index="/admin/users">👤 用户管理</el-menu-item>
        <el-menu-item index="/admin/merchants">🏪 商家管理</el-menu-item>
        <el-menu-item index="/admin/products">📦 商品审核</el-menu-item>
        <el-menu-item index="/admin/activities">🎯 活动管理</el-menu-item>
        <el-menu-item index="/admin/venues">🏟️ 会场管理</el-menu-item>
        <el-menu-item index="/admin/roles">🔐 角色权限</el-menu-item>
        <el-menu-item index="/admin/dict">📋 数据字典</el-menu-item>
        <el-menu-item index="/admin/violations">⚠️ 违规处罚</el-menu-item>
        <el-menu-item index="/admin/config">⚙️ 系统配置</el-menu-item>
      </el-menu>
    </el-aside>
    <el-main style="background:#f0f2f5;padding:20px">
      <h2>🏟️ 活动会场管理</h2>
      <el-button type="primary" @click="dialog=true" style="margin-bottom:16px">创建会场</el-button>
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60"/>
        <el-table-column prop="name" label="会场名称"/>
        <el-table-column prop="description" label="描述" width="200"/>
        <el-table-column prop="startTime" label="开始时间" width="160"/>
        <el-table-column prop="endTime" label="结束时间" width="160"/>
        <el-table-column label="状态" width="100">
          <template #default="{row}">{{ ['草稿','已发布','已下线'][row.status] || '未知' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="{row}">
            <el-button v-if="row.status===0" size="small" type="success" @click="setStatus(row.id,1)">发布</el-button>
            <el-button v-if="row.status===1" size="small" type="warning" @click="setStatus(row.id,2)">下线</el-button>
            <el-button size="small" @click="editVenue(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="delVenue(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-dialog v-model="dialog" title="编辑会场" width="600px">
        <el-form :model="form" label-width="100px">
          <el-form-item label="会场名称"><el-input v-model="form.name"/></el-form-item>
          <el-form-item label="描述"><el-input v-model="form.description" type="textarea"/></el-form-item>
          <el-form-item label="页面配置JSON">
            <el-input v-model="form.pageConfig" type="textarea" rows="10" placeholder='{"banner":{"text":"双11大促","link":"/seckill"},"productIds":[1,2,3],"couponIds":[1,2],"background":"#ff4d4f"}'/>
            <span style="color:#999;font-size:12px">配置项：banner(text/link), productIds[], couponIds[], background(背景色)</span>
          </el-form-item>
          <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" placeholder="开始时间" style="width:100%"/></el-form-item>
          <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" placeholder="结束时间" style="width:100%"/></el-form-item>
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
const form = ref<any>({ name:'', description:'', pageConfig:'', startTime:'', endTime:'' })
const editId = ref<number | null>(null)

async function load() { const r:any = await request.get('/venue'); list.value = r.data || [] }
async function save() {
  const data = {
    ...form.value,
    startTime: form.value.startTime ? new Date(form.value.startTime).toISOString().replace('T',' ').substring(0,19) : null,
    endTime: form.value.endTime ? new Date(form.value.endTime).toISOString().replace('T',' ').substring(0,19) : null,
  }
  if (editId.value) await request.put('/venue/' + editId.value, data)
  else await request.post('/venue', data)
  ElMessage.success('保存成功'); dialog.value = false; editId.value = null
  form.value = { name:'', description:'', pageConfig:'', startTime:'', endTime:'' }
  load()
}
function editVenue(row: any) { editId.value = row.id; form.value = { ...row }; dialog.value = true }
async function setStatus(id: number, status: number) { await request.put('/venue/' + id + '/status', { status }); load() }
async function delVenue(id: number) { await request.delete('/venue/' + id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
