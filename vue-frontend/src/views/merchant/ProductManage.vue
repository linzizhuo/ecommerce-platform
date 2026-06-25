<template>
  <div class="page"><el-container>
    <el-header class="mh"><span>CloudMall 商家后台</span>
      <div><router-link to="/merchant/dashboard">看板</router-link><router-link to="/merchant/products">商品</router-link><router-link to="/merchant/orders">订单</router-link></div>
    </el-header>
    <el-main>
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
        <h3>商品管理</h3>
        <el-button type="primary" @click="$router.push('/merchant/product/edit')">+ 发布商品</el-button>
      </div>
      <el-table :data="products">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="brand" label="品牌" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{row}"><el-tag :type="row.status===2?'success':'info'">{{ row.status===2?'已上架':'草稿' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{row}"><el-button size="small" @click="$router.push(`/merchant/product/edit/${row.id}`)">编辑</el-button><el-button size="small" type="danger" @click="del(row.id)">删除</el-button></template>
        </el-table-column>
      </el-table>
    </el-main>
  </el-container></div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'; import request from '@/utils/request'
const products = ref<any[]>([])
async function load() { const r:any = await request.get('/merchant/products'); products.value = r.data||[] }
async function del(id:number) { await request.delete(`/merchant/product/${id}`); load() }
onMounted(load)
</script>
<style scoped>.page{min-height:100vh;background:#f0f2f5}.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}.mh a{margin:0 10px;color:#666;text-decoration:none;font-size:14px;font-weight:400}.el-main{max-width:1200px;margin:20px auto}</style>
