<template>
  <div class="cart-page">
    <header class="top-bar"><div class="inner"><router-link to="/" class="logo">CloudMall</router-link></div></header>
    <div class="inner">
      <h2>购物车</h2>
      <el-table :data="items" style="width:100%" @selection-change="handleSelect">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="specInfo" label="规格" width="150" />
        <el-table-column label="单价" width="120"><template #default="{row}">¥{{ (row.price/100).toFixed(2) }}</template></el-table-column>
        <el-table-column label="数量" width="150">
          <template #default="{row}">
            <el-input-number v-model="row.quantity" :min="1" size="small" @change="updateQty(row)" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120"><template #default="{row}">¥{{ (row.price*row.quantity/100).toFixed(2) }}</template></el-table-column>
        <el-table-column label="操作" width="80"><template #default="{row}"><el-button type="danger" size="small" @click="removeItem(row)">删除</el-button></template></el-table-column>
      </el-table>
      <div style="text-align:right;margin-top:20px">
        <span style="font-size:18px;color:#ff4d4f">合计: ¥{{ total.toFixed(2) }}</span>
        <el-button type="danger" size="large" style="margin-left:20px" @click="goCheckout" :disabled="!selected.length">去结算({{ selected.length }})</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const items = ref<any[]>([])
const selected = ref<any[]>([])

const total = computed(() => selected.value.reduce((s, i) => s + i.price * i.quantity, 0) / 100)

function handleSelect(v: any[]) { selected.value = v }

async function updateQty(row: any) {
  await request.put(`/cart/${row.skuId}`, { quantity: row.quantity })
}

async function removeItem(row: any) {
  await request.delete(`/cart/${row.skuId}`)
  loadCart()
}

function goCheckout() {
  localStorage.setItem('checkout_items', JSON.stringify(selected.value))
  router.push('/checkout')
}

async function loadCart() {
  const res: any = await request.get('/cart')
  items.value = res.data || []
}

onMounted(loadCart)
</script>

<style scoped>
.cart-page { min-height: 100vh; background: #f5f5f5; }
.top-bar { background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.inner { max-width: 1200px; margin: 0 auto; padding: 20px; }
.logo { font-size: 24px; font-weight: bold; color: #ff4d4f; text-decoration: none; }
</style>
