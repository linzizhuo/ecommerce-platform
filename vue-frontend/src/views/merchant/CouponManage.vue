<template>
  <div class="page"><el-container>
    <el-header class="mh"><span>CloudMall 商家后台</span>
      <div><router-link to="/merchant/dashboard">看板</router-link><router-link to="/merchant/products">商品</router-link><router-link to="/merchant/orders">订单</router-link><router-link to="/merchant/coupons">优惠券</router-link></div>
    </el-header>
    <el-main>
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
        <h3>优惠券管理</h3>
        <el-button type="primary" @click="openCreate">+ 新建优惠券</el-button>
      </div>
      <el-table :data="coupons" v-if="coupons.length">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" />
        <el-table-column label="类型" width="100">
          <template #default="{row}">{{ row.type===1?'满减券':'折扣券' }}</template>
        </el-table-column>
        <el-table-column label="门槛" width="120">
          <template #default="{row}">¥{{ (row.threshold/100).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="优惠" width="120">
          <template #default="{row}">¥{{ (row.value/100).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="已领/总量" width="100">
          <template #default="{row}">{{ row.receivedCount||0 }}/{{ row.totalCount }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{row}"><el-button type="danger" size="small" @click="del(row.id)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无优惠券，点击上方按钮创建" />

      <!-- 新建弹窗 -->
      <el-dialog v-model="visible" title="新建优惠券" width="450px">
        <el-form :model="form" label-width="80px">
          <el-form-item label="名称"><el-input v-model="form.name" placeholder="如: 618满减券" /></el-form-item>
          <el-form-item label="类型">
            <el-radio-group v-model="form.type"><el-radio :value="1">满减券</el-radio><el-radio :value="2">折扣券</el-radio></el-radio-group>
          </el-form-item>
          <el-form-item label="使用门槛">
            <el-input-number v-model="form.threshold" :min="0" /> 分 (0=无门槛)
          </el-form-item>
          <el-form-item label="优惠金额">
            <el-input-number v-model="form.value" :min="1" /> 分
          </el-form-item>
          <el-form-item label="发行数量">
            <el-input-number v-model="form.totalCount" :min="1" :max="10000" />
          </el-form-item>
        </el-form>
        <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="create">创建</el-button></template>
      </el-dialog>
    </el-main>
  </el-container></div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'; import { ElMessage } from 'element-plus'; import request from '@/utils/request'
const coupons = ref<any[]>([]); const visible = ref(false)
const form = reactive({ name:'', type:1, threshold:0, value:1000, totalCount:100 })
async function load() { const r:any = await request.get('/coupon/merchant'); coupons.value = r.data||[] }
function openCreate() { Object.assign(form, {name:'',type:1,threshold:0,value:1000,totalCount:100}); visible.value = true }
async function create() { await request.post('/coupon/merchant', form); ElMessage.success('创建成功'); visible.value=false; load() }
async function del(id:number) { await request.delete(`/coupon/merchant/${id}`); load() }
onMounted(load)
</script>
<style scoped>.page{min-height:100vh;background:#f0f2f5}.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}.mh a{margin:0 10px;color:#666;text-decoration:none;font-size:14px;font-weight:400}.el-main{max-width:1200px;margin:20px auto}</style>
