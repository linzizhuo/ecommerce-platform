<template>
  <div class="page">
    <el-container>
      <el-header class="m-header">
        <span class="title">CloudMall 商家后台</span>
        <div><router-link to="/merchant/dashboard">看板</router-link>
          <router-link to="/merchant/products">商品</router-link>
          <router-link to="/merchant/orders">订单</router-link>
          <router-link to="/merchant/coupons">优惠券</router-link>
          <el-button size="small" @click="$router.push('/')">回前台</el-button></div>
      </el-header>
      <el-main>
        <el-button type="primary" @click="$router.push('/merchant/product/edit')">发布商品</el-button>
        <el-table :data="products" style="margin-top:15px">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="商品名称" />
          <el-table-column prop="brand" label="品牌" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{row}">
              <el-tag v-if="row.status===2" type="success">已上架</el-tag>
              <el-tag v-else-if="row.status===1">待审核</el-tag>
              <el-tag v-else type="info">草稿</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{row}">
              <el-button size="small" @click="$router.push(`/merchant/product/edit/${row.id}`)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteProduct(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-main>
    </el-container>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
const products = ref<any[]>([])
onMounted(async () => {
  const res: any = await request.get('/product/merchant/list')
  products.value = res.data || []
})
async function deleteProduct(id: number) {
  await request.delete(`/product/merchant/${id}`)
  products.value = products.value.filter(p => p.id !== id)
}
</script>
<style scoped>.page { min-height: 100vh; background: #f5f5f5; }
.m-header { background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.08); display: flex; align-items: center; justify-content: space-between; }
.title { font-size: 20px; font-weight: bold; color: #1890ff; }
.m-header a { margin: 0 10px; color: #666; text-decoration: none; }
.el-main { max-width: 1200px; margin: 20px auto; }</style>
