<template>
  <div class="page">
    <MerchantLayout>
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
    </MerchantLayout>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import MerchantLayout from '@/layouts/MerchantLayout.vue'
import request from '@/utils/request'

const list = ref<any[]>([])
async function load() { const r: any = await request.get('/merchant/stock-report'); list.value = r.data || [] }
onMounted(load)
</script>
.page { min-height: 100vh; background: #f5f5f5; }
.mh a.router-link-active{color:#1890ff}
.el-main { max-width: 1200px; margin: 20px auto; }
