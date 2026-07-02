<template>
  <el-container style="min-height:100vh">
    <el-header class="mh"><span>CloudMall 商家后台</span>
      <div><router-link to="/merchant/dashboard">看板</router-link><router-link to="/merchant/products">商品</router-link><router-link to="/merchant/orders">订单</router-link><router-link to="/merchant/coupons">优惠券</router-link><router-link to="/merchant/seckill">秒杀</router-link><router-link to="/merchant/groupbuy">拼团</router-link><router-link to="/merchant/presale">预售</router-link><router-link to="/merchant/distribution">分销</router-link><router-link to="/merchant/combo">套餐</router-link><router-link to="/merchant/redenvelope">红包</router-link><router-link to="/" style="color:#ff4d4f">回前台</router-link></div>
    </el-header>
    <el-main style="max-width:1200px;margin:0 auto;padding:20px">
      <el-button type="primary" @click="showDialog()" style="margin-bottom:16px">创建套餐</el-button>
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="name" label="套餐名称"/>
        <el-table-column prop="description" label="描述"/>
        <el-table-column label="原价(元)"><template #default="{row}">¥{{(row.totalPrice/100).toFixed(2)}}</template></el-table-column>
        <el-table-column label="套餐价(元)"><template #default="{row}">¥{{(row.discountPrice/100).toFixed(2)}}</template></el-table-column>
        <el-table-column label="包含商品" width="200">
          <template #default="{row}"><el-tag v-for="item in row.items" :key="item.id" size="small" style="margin:2px">SKU#{{item.skuId}} x{{item.quantity}}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{row}"><el-button type="danger" size="small" @click="del(row.id)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <el-dialog v-model="dialog" title="创建套餐" width="500px">
        <el-form :model="form" label-width="100px">
          <el-form-item label="套餐名称"><el-input v-model="form.name"/></el-form-item>
          <el-form-item label="描述"><el-input v-model="form.description" type="textarea"/></el-form-item>
          <el-form-item label="原价(元)"><el-input v-model="form.totalPrice"/></el-form-item>
          <el-form-item label="套餐价(元)"><el-input v-model="form.discountPrice"/></el-form-item>
          <el-form-item label="包含SKU">
            <div v-for="(item,idx) in form.items" :key="idx" style="display:flex;gap:8px;margin-bottom:4px">
              <el-input v-model="item.skuId" placeholder="SKU ID" style="width:120px"/>
              <el-input-number v-model="item.quantity" :min="1" placeholder="数量" style="width:80px"/>
              <el-button @click="form.items.splice(idx,1)" type="danger" size="small">-</el-button>
            </div>
            <el-button size="small" @click="form.items.push({skuId:'',quantity:1})">+ 添加SKU</el-button>
          </el-form-item>
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
const form = ref({ name:'', description:'', totalPrice:'', discountPrice:'', items:[] as any[] })

async function load() { const r: any = await request.get('/merchant/combo/list'); list.value = r.data || [] }
function showDialog() { form.value = { name:'', description:'', totalPrice:'', discountPrice:'', items:[] }; dialog.value = true }
async function save() {
  await request.post('/merchant/combo', form.value)
  ElMessage.success('创建成功'); dialog.value = false; load()
}
async function del(id: number) { await request.delete('/merchant/combo/' + id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
<style scoped>
.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
.mh a{margin:0 10px;color:#666;text-decoration:none;font-size:14px;font-weight:400}
</style>
