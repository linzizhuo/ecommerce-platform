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
        <h3>📦 库存报表</h3>
        <el-table :data="list" stripe>
          <el-table-column prop="productId" label="商品ID" width="100"/>
          <el-table-column prop="productName" label="商品名称"/>
          <el-table-column prop="skuCount" label="SKU数" width="80"/>
          <el-table-column prop="totalStock" label="总库存" width="100"/>
          <el-table-column label="状态" width="120">
            <template #default="{row}">
              <el-tag v-if="row.lowStockAlert" type="danger">⚠ 库存不足</el-tag>
              <el-tag v-else type="success">正常</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="SKU明细">
            <template #default="{row}">
              <div v-for="s in row.skus" :key="s.id" style="margin:2px 0">
                <el-tag size="small" :type="s.stock<10?'danger':''">
                  {{ s.specInfo || '默认' }}: 库存{{ s.stock }} | ¥{{ (s.price/100).toFixed(2) }}
                </el-tag>
              </div>
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

const list = ref<any[]>([])
async function load() { const r: any = await request.get('/merchant/stock-report'); list.value = r.data || [] }
onMounted(load)
</script>
<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }
.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
.mh a{margin:0 8px;color:#666;text-decoration:none;font-size:13px;font-weight:400}
.mh a.router-link-active{color:#1890ff}
.el-main { max-width: 1200px; margin: 20px auto; }
</style>
