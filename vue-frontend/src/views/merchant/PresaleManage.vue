<template>
  <el-container style="min-height:100vh">
    <el-header class="mh"><span>CloudMall 商家后台</span>
      <div><router-link to="/merchant/dashboard">看板</router-link><router-link to="/merchant/products">商品</router-link><router-link to="/merchant/orders">订单</router-link><router-link to="/merchant/coupons">优惠券</router-link><router-link to="/merchant/seckill">秒杀</router-link><router-link to="/merchant/groupbuy">拼团</router-link><router-link to="/merchant/presale">预售</router-link><router-link to="/merchant/distribution">分销</router-link><router-link to="/merchant/combo">套餐</router-link><router-link to="/merchant/redenvelope">红包</router-link><router-link to="/" style="color:#ff4d4f">回前台</router-link></div>
    </el-header>
    <el-main style="max-width:1200px;margin:0 auto;padding:20px">
      <el-button type="warning" @click="dialog=true" style="margin-bottom:16px">创建预售活动</el-button>
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="productId" label="商品ID"/>
        <el-table-column label="定金"><template #default="{row}">¥{{(row.deposit/100).toFixed(2)}}</template></el-table-column>
        <el-table-column label="尾款"><template #default="{row}">¥{{(row.finalAmount/100).toFixed(2)}}</template></el-table-column>
        <el-table-column prop="depositStart" label="定金开始" width="170"/>
        <el-table-column prop="depositEnd" label="定金结束" width="170"/>
        <el-table-column prop="finalStart" label="尾款开始" width="170"/>
        <el-table-column prop="finalEnd" label="尾款结束" width="170"/>
        <el-table-column label="操作" width="100">
          <template #default="{row}"><el-button type="danger" size="small" @click="del(row.id)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <el-dialog v-model="dialog" title="创建预售活动" width="500px">
        <el-form :model="form" label-width="100px">
          <el-form-item label="商品ID"><el-input v-model="form.productId"/></el-form-item>
          <el-form-item label="SKU ID"><el-input v-model="form.skuId"/></el-form-item>
          <el-form-item label="定金(元)"><el-input v-model="form.deposit"/></el-form-item>
          <el-form-item label="尾款(元)"><el-input v-model="form.finalAmount"/></el-form-item>
          <el-form-item label="定金开始"><el-input v-model="form.depositStart" placeholder="2026-06-01 00:00:00"/></el-form-item>
          <el-form-item label="定金结束"><el-input v-model="form.depositEnd" placeholder="2026-06-15 23:59:59"/></el-form-item>
          <el-form-item label="尾款开始"><el-input v-model="form.finalStart"/></el-form-item>
          <el-form-item label="尾款结束"><el-input v-model="form.finalEnd"/></el-form-item>
        </el-form>
        <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="warning" @click="save">保存</el-button></template>
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
const form = ref({ productId:'', skuId:'', deposit:'', finalAmount:'', depositStart:'', depositEnd:'', finalStart:'', finalEnd:'' })

async function load() { const r: any = await request.get('/merchant/presale/list'); list.value = r.data || [] }
async function save() {
  await request.post('/merchant/presale', form.value)
  ElMessage.success('创建成功'); dialog.value = false; load()
}
async function del(id: number) { await request.delete('/merchant/presale/' + id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
<style scoped>
.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
.mh a{margin:0 10px;color:#666;text-decoration:none;font-size:14px;font-weight:400}
</style>
