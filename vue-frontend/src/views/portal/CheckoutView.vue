<template>
  <div class="checkout-page">
    <header class="top-bar"><div class="top-inner"><router-link to="/" class="logo">CloudMall</router-link></div></header>
    <div class="content-inner">
      <h2>确认订单</h2>
      <el-card class="section">
        <template #header>收货地址</template>
        <el-radio-group v-model="addressId">
          <el-radio v-for="a in addresses" :key="a.id" :value="a.id" style="display:block;margin:5px 0">
            {{ a.receiver }} {{ a.phone }} {{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}
          </el-radio>
        </el-radio-group>
      </el-card>
      <el-card class="section">
        <template #header>商品信息</template>
        <el-table :data="items">
          <el-table-column prop="productName" label="商品" />
          <el-table-column label="规格"><template #default="{row}">{{ fmtSpec(row.specInfo) }}</template></el-table-column>
          <el-table-column label="单价"><template #default="{row}">¥{{ (row.price/100).toFixed(2) }}</template></el-table-column>
          <el-table-column prop="quantity" label="数量" />
        </el-table>
      </el-card>
      <el-card class="section">
        <template #header>优惠券 <el-button size="small" text @click="loadCoupons">刷新</el-button></template>
        <el-radio-group v-model="selectedCouponId">
          <el-radio :value="null">不使用优惠券</el-radio>
          <el-radio v-for="c in coupons" :key="c.id" :value="c.id" style="display:block;margin:3px 0">
            {{ c.name }} —
            <template v-if="c.type===2">{{ (c.value/10).toFixed(1) }}折券</template>
            <template v-else>满¥{{ (c.threshold/100).toFixed(0) }}减¥{{ (c.value/100).toFixed(0) }}</template>
            <span v-if="total < c.threshold" style="color:#ff4d4f"> (未满门槛)</span>
          </el-radio>
        </el-radio-group>
        <p v-if="coupons.length===0" style="color:#999">暂无可用优惠券</p>
      </el-card>
      <el-card class="section">
        <template #header>支付方式</template>
        <el-radio-group v-model="payMethod">
          <el-radio :value="1">支付宝</el-radio>
          <el-radio :value="2">微信支付</el-radio>
        </el-radio-group>
      </el-card>
      <div class="total-bar">
        <span v-if="discount>0" style="color:#999">优惠: -¥{{ (discount/100).toFixed(2) }}</span>
        <span class="total-price">实付: ¥{{ (total - discount).toFixed(2) }}</span>
        <el-button type="danger" size="large" @click="submitOrder" :loading="submitting">提交订单</el-button>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'; import { useRouter } from 'vue-router'; import { ElMessage } from 'element-plus'; import request from '@/utils/request'
const router = useRouter()
const addresses = ref<any[]>([]); const addressId = ref<number|null>(null)
const payMethod = ref(1); const items = ref<any[]>([]); const submitting = ref(false)
const coupons = ref<any[]>([]); const selectedCouponId = ref<number|null>(null)
const total = computed(() => items.value.reduce((s,i) => s + i.price * i.quantity, 0))
const discount = computed(() => {
  if (!selectedCouponId.value) return 0
  const c = coupons.value.find(c => c.id === selectedCouponId.value)
  if (!c || total.value < c.threshold) return 0
  if (c.type === 1) return c.value  // 满减券: 直接减
  if (c.type === 2) return Math.floor(total.value * (100 - c.value) / 100)  // 折扣券: 8.5折=value85
  return 0
})
function fmtSpec(s: string): string { try { return Object.values(JSON.parse(s)).join(' / ') } catch { return s } }
async function loadCoupons() {
  try { const r:any = await request.get('/coupon/my'); coupons.value = r.data||[] } catch {}
}
async function submitOrder() {
  if (!addressId.value) { ElMessage.warning('请选择地址'); return }
  const addr = addresses.value.find(a => a.id === addressId.value)
  submitting.value = true
  try {
    const res: any = await request.post('/order', { addressId: addressId.value, addressSnapshot: JSON.stringify(addr), payMethod: payMethod.value, couponId: selectedCouponId.value })
    ElMessage.success('下单成功，请支付!'); router.push('/pay/' + res.data.orderId)
  } finally { submitting.value = false }
}
onMounted(async () => {
  const cached = localStorage.getItem('checkout_items')
  if (cached) items.value = JSON.parse(cached); else router.push('/cart')
  try { const r:any = await request.get('/address'); addresses.value=r.data||[]; if (addresses.value.length) addressId.value=addresses.value[0].id } catch {}
  loadCoupons()
})
</script>
<style scoped>
.checkout-page{min-height:100vh;background:#f0f2f5}.top-bar{background:#fff;box-shadow:0 1px 4px rgba(0,0,0,.06)}.top-inner{display:flex;align-items:center;max-width:800px;margin:0 auto;padding:12px 20px}.content-inner{max-width:800px;margin:0 auto;padding:20px}.logo{font-size:22px;font-weight:700;color:#ff4d4f;text-decoration:none}.section{margin-bottom:16px}.total-bar{text-align:right;padding:20px;background:#fff;border-radius:8px;display:flex;justify-content:flex-end;align-items:center;gap:16px}.total-price{font-size:24px;color:#ff4d4f;font-weight:700}
</style>
