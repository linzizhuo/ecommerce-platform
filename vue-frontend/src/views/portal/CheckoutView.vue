<template>
  <div class="checkout-page">
    <header class="top-bar"><div class="inner"><router-link to="/" class="logo">CloudMall</router-link></div></header>
    <div class="inner">
      <h2>确认订单</h2>
      <el-card style="margin-bottom:20px">
        <template #header>收货地址</template>
        <el-radio-group v-model="addressId">
          <el-radio v-for="a in addresses" :key="a.id" :value="a.id" style="display:block;margin:5px 0">
            {{ a.receiver }} {{ a.phone }} {{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}
          </el-radio>
        </el-radio-group>
      </el-card>
      <el-card style="margin-bottom:20px">
        <template #header>商品信息</template>
        <el-table :data="items">
          <el-table-column prop="productName" label="商品" />
          <el-table-column label="规格"><template #default="{row}">{{ fmtSpec(row.specInfo) }}</template></el-table-column>
          <el-table-column label="单价"><template #default="{row}">¥{{ (row.price/100).toFixed(2) }}</template></el-table-column>
          <el-table-column prop="quantity" label="数量" />
        </el-table>
      </el-card>
      <el-card>
        <template #header>支付方式</template>
        <el-radio-group v-model="payMethod">
          <el-radio :value="1">支付宝</el-radio>
          <el-radio :value="2">微信支付</el-radio>
        </el-radio-group>
      </el-card>
      <div style="text-align:right;margin-top:20px">
        <span style="font-size:20px;color:#ff4d4f">实付: ¥{{ total.toFixed(2) }}</span>
        <el-button type="danger" size="large" style="margin-left:20px" @click="submitOrder" :loading="submitting">提交订单</el-button>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
const router = useRouter()
const addresses = ref<any[]>([]); const addressId = ref<number|null>(null)
const payMethod = ref(1); const items = ref<any[]>([]); const submitting = ref(false)
const total = computed(() => items.value.reduce((s,i) => s + i.price * i.quantity, 0) / 100)
function fmtSpec(s: string): string { try { return Object.values(JSON.parse(s)).join(' / ') } catch { return s } }
async function submitOrder() {
  if (!addressId.value) { ElMessage.warning('请选择地址'); return }
  const addr = addresses.value.find(a => a.id === addressId.value)
  submitting.value = true
  try {
    const res: any = await request.post('/order', { addressId: addressId.value, addressSnapshot: JSON.stringify(addr), payMethod: payMethod.value })
    await request.post(`/order/${res.data.orderId}/pay`, { payMethod: payMethod.value })
    ElMessage.success('支付成功!'); router.push('/orders')
  } finally { submitting.value = false }
}
onMounted(async () => {
  const cached = localStorage.getItem('checkout_items')
  if (cached) items.value = JSON.parse(cached); else router.push('/cart')
  try { const res: any = await request.get('/address'); addresses.value = res.data||[]; if (addresses.value.length) addressId.value = addresses.value[0].id } catch {}
})
</script>
<style scoped>.checkout-page{min-height:100vh;background:#f5f5f5}.top-bar{background:#fff;box-shadow:0 2px 8px rgba(0,0,0,0.08)}.inner{max-width:800px;margin:0 auto;padding:20px}.logo{font-size:24px;font-weight:bold;color:#ff4d4f;text-decoration:none}</style>
