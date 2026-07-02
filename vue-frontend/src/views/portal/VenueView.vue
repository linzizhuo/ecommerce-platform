<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">{{ venue?.name || '活动会场' }}</div>
    </div></header>
    <div class="container" v-if="venue">
      <!-- Banner -->
      <div class="venue-banner" :style="{background: config.banner?.background || config.background || '#ff4d4f'}">
        <h1>{{ config.banner?.text || venue.name }}</h1>
        <p>{{ venue.description }}</p>
        <el-button v-if="config.banner?.link" type="default" size="large" @click="$router.push(config.banner.link)">立即前往</el-button>
      </div>
      <!-- 商品区 -->
      <div v-if="config.productIds?.length" style="margin:20px 0">
        <h3>🔥 推荐商品</h3>
        <div class="product-grid">
          <el-card v-for="p in products" :key="p.id" shadow="hover" @click="$router.push('/product/'+p.id)" class="product-card">
            <h4>{{ p.name }}</h4>
            <p class="brand">{{ p.brand }}</p>
          </el-card>
        </div>
      </div>
      <!-- 优惠券区 -->
      <div v-if="config.couponIds?.length" style="margin:20px 0">
        <h3>🎫 会场专属优惠券</h3>
        <div style="display:flex;gap:12px;flex-wrap:wrap">
          <el-card v-for="c in coupons" :key="c.id" class="coupon-card">
            <div class="coupon-value">¥{{ (c.value/100).toFixed(0) }}</div>
            <small>满¥{{ (c.threshold/100).toFixed(0) }}可用</small>
          </el-card>
        </div>
      </div>
    </div>
    <el-empty v-else description="会场不存在或已下线" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const venue = ref<any>(null)
const products = ref<any[]>([])
const coupons = ref<any[]>([])

const config = computed(() => {
  if (!venue.value?.pageConfig) return {}
  try { return JSON.parse(venue.value.pageConfig) } catch { return {} }
})

async function load() {
  const id = route.params.id
  const r: any = await request.get('/venue/' + id)
  venue.value = r.data
  if (venue.value) {
    const cfg = config.value
    if (cfg.productIds?.length) {
      const pRes: any = await request.get('/product/list', { params: { ids: cfg.productIds.join(',') } })
      products.value = pRes.data || []
    }
    if (cfg.couponIds?.length) {
      // Try to get coupon details from available coupons
      try {
        const cRes: any = await request.get('/coupon/available')
        const allCoupons = cRes.data || []
        coupons.value = allCoupons.filter((c: any) => cfg.couponIds.includes(c.id))
      } catch {}
    }
  }
}
onMounted(load)
</script>
<style scoped>
.page{background:#f0f2f5;min-height:100vh}
.top-bar{background:#fff;box-shadow:0 1px 4px rgba(0,0,0,.06)}
.inner{max-width:1200px;margin:0 auto;padding:0 20px}
.top-row{display:flex;align-items:center;gap:16px;height:60px}
.logo{font-size:22px;font-weight:700;color:#ff4d4f;text-decoration:none}
.title{font-size:18px;font-weight:600;flex:1}
.container{max-width:1000px;margin:0 auto;padding:20px}
.venue-banner{padding:60px 40px;text-align:center;color:#fff;border-radius:12px;margin-bottom:20px}
.venue-banner h1{font-size:36px;margin:0 0 12px}
.product-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}
.product-card{cursor:pointer;border-radius:8px}
.product-card:hover{transform:translateY(-2px)}
.brand{color:#999;font-size:13px;margin-top:4px}
.coupon-card{text-align:center;width:150px}
.coupon-value{font-size:24px;font-weight:700;color:#ff4d4f}
</style>
