<template>
  <div class="page">
    <el-container>
      <el-header class="mh"><span>CloudMall 商家后台</span>
        <div>
          <router-link to="/merchant/dashboard">看板</router-link>
          <router-link to="/merchant/products">商品</router-link>
          <router-link to="/merchant/orders">订单</router-link>
          <router-link to="/merchant/coupons">优惠券</router-link>
          <router-link to="/merchant/seckill">秒杀</router-link>
          <router-link to="/merchant/groupbuy">拼团</router-link>
          <router-link to="/merchant/presale">预售</router-link>
          <router-link to="/merchant/distribution">分销</router-link>
          <router-link to="/merchant/combo">套餐</router-link>
          <router-link to="/merchant/redenvelope">红包</router-link>
          <router-link to="/merchant/activities">活动</router-link>
          <router-link to="/merchant/reconciliation">对账</router-link>
          <router-link to="/merchant/stock-report">库存</router-link>
          <router-link to="/" style="color:#ff4d4f">回前台</router-link>
        </div>
      </el-header>
      <el-main>
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
      </el-main>
    </el-container>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
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
<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }
.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
.mh a{margin:0 8px;color:#666;text-decoration:none;font-size:13px;font-weight:400}
.mh a.router-link-active{color:#1890ff}
.el-main { max-width: 1200px; margin: 20px auto; }
</style>
