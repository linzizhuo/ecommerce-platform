<template>
  <div class="detail-page">
    <header class="top-bar"><div class="inner">
      <router-link to="/" class="logo">CloudMall</router-link>
      <div style="flex:1" />
      <router-link to="/cart" class="cart-link"><el-icon :size="22"><ShoppingCart /></el-icon></router-link>
    </div></header>
    <div class="content" v-if="product">
      <div class="detail-layout">
        <div class="main-img"><div class="img-placeholder" :style="{background: gradient}">{{ product.name?.charAt(0) }}</div></div>
        <div class="info">
          <h2>{{ product.name }} <el-button size="small" :type="faved?'danger':'default'" circle @click="toggleFav">{{ faved ? '❤️' : '🤍' }}</el-button></h2>
          <p class="brand">品牌: {{ product.brand }}</p>
          <div class="rating-row" v-if="reviewData.avgRating > 0">
            <el-rate v-model="reviewData.avgRating" disabled show-score text-color="#ff9900" />
            <span class="review-count">{{ reviewData.total }}条评价</span>
          </div>
          <el-divider />
          <h4>选择规格</h4>
          <div class="sku-group">
            <el-button v-for="sku in skuList" :key="sku.id" :type="selectedSkuId === sku.id ? 'danger' : 'default'"
              @click="selectedSkuId = sku.id" style="margin:0 8px 8px 0">
              {{ formatSpec(sku.specInfo) }} - ¥{{ (sku.price / 100).toFixed(2) }}
            </el-button>
          </div>
          <p v-if="selectedSku" class="price">¥{{ (selectedSku.price / 100).toFixed(2) }}
            <span class="orig">¥{{ (selectedSku.originalPrice / 100).toFixed(2) }}</span>
          </p>
          <p class="stock">库存: {{ selectedSku?.stock || 0 }} 件</p>
          <div class="qty-row">
            <span>数量</span>
            <el-input-number v-model="quantity" :min="1" :max="selectedSku?.stock || 1" />
          </div>
          <div class="action-row">
            <el-button type="danger" size="large" @click="addToCart" round>🛒 加入购物车</el-button>
            <el-button type="warning" size="large" @click="buyNow" round>⚡ 立即购买</el-button>
          </div>
        </div>
      </div>
      <!-- 商品描述 -->
      <el-card v-if="product.description" class="desc-section">
        <template #header><span style="font-weight:bold">📝 商品详情</span></template>
        <div class="desc-content" v-html="product.description"></div>
      </el-card>
      <!-- 评价区 -->
      <el-card class="review-section">
        <template #header><span style="font-weight:bold">商品评价 ({{ reviewData.total }})</span>
          <span style="color:#ff4d4f;margin-left:10px">⭐ {{ reviewData.avgRating }}分</span>
        </template>
        <div v-if="reviewData.list?.length">
          <div v-for="r in reviewData.list" :key="r.id" class="review-item">
            <div class="review-header">
              <el-rate v-model="r.rating" disabled size="small" />
              <span class="review-time">{{ r.createTime }}</span>
            </div>
            <p v-if="r.content">{{ r.content }}</p>
            <p v-else style="color:#ccc">默认好评</p>
          </div>
        </div>
        <el-empty v-else description="暂无评价，快来第一个评价吧~" :image-size="80" />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/userStore'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'

const route = useRoute(); const router = useRouter()
const userStore = useUserStore()
const product = ref<any>(null); const skuList = ref<any[]>([])
const selectedSkuId = ref<number | null>(null); const quantity = ref(1)
const reviewData = ref<any>({ list: [], avgRating: 0, total: 0 })
const faved = ref(false)
const gradient = 'linear-gradient(135deg, ' + ['#667eea,#764ba2','#f093fb,#f5576c','#4facfe,#00f2fe','#43e97b,#38f9d7'][Math.floor(Math.random()*4)] + ')'

const selectedSku = computed(() => skuList.value.find(s => s.id === selectedSkuId.value))

function formatSpec(s: string): string {
  try { return Object.values(JSON.parse(s)).join(' / ') } catch { return s }
}
async function addToCart() {
  if (!userStore.token) { router.push('/login'); return }
  if (!selectedSku.value) { ElMessage.warning('请选择规格'); return }
  await request.post('/cart', { skuId: selectedSku.value.id, productName: product.value.name, specInfo: selectedSku.value.specInfo, price: selectedSku.value.price, quantity: quantity.value, image: '' })
  ElMessage.success('已加入购物车')
}
function buyNow() { addToCart().then(() => router.push('/cart')) }
async function toggleFav() {
  if (!userStore.token) { router.push('/login'); return }
  const pid = Number(route.params.id)
  if (faved.value) { await removeFavorite(pid); ElMessage.success('已取消收藏') }
  else { await addFavorite(pid); ElMessage.success('已收藏') }
  faved.value = !faved.value
}

onMounted(async () => {
  const id = route.params.id
  const res: any = await request.get(`/product/${id}`)
  product.value = res.data?.product || res.data
  skuList.value = res.data?.skuList || []
  if (skuList.value.length) selectedSkuId.value = skuList.value[0].id
  // 加载评价 & 收藏状态
  try {
    const rev: any = await request.get(`/review/product/${id}`)
    reviewData.value = rev.data || { list: [], avgRating: 0, total: 0 }
  } catch {}
  try { const f: any = await checkFavorite(Number(id)); faved.value = f.data || false } catch {}
})
</script>

<style scoped>
.detail-page { min-height: 100vh; background: #f0f2f5; }
.top-bar { background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); padding: 0 20px; position: sticky; top: 0; z-index: 100; }
.top-bar .inner { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; height: 60px; }
.logo { font-size: 22px; font-weight: 700; color: #ff4d4f; text-decoration: none; }
.cart-link { color: #555; padding: 8px; border-radius: 50%; transition: background 0.2s; }
.cart-link:hover { background: #f5f5f5; }
.content { max-width: 1100px; margin: 0 auto; padding: 24px 20px; }
.detail-layout { display: flex; gap: 32px; background: #fff; padding: 28px; border-radius: 12px; }
.main-img { width: 420px; flex-shrink: 0; }
.img-placeholder { width: 100%; height: 400px; display: flex; align-items: center; justify-content: center; font-size: 80px; color: rgba(255,255,255,0.85); font-weight: bold; border-radius: 12px; }
.info { flex: 1; }
.info h2 { font-size: 22px; margin-bottom: 6px; }
.brand { color: #999; margin-bottom: 10px; }
.rating-row { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.review-count { color: #999; font-size: 13px; }
.sku-group { margin: 10px 0; }
.price { font-size: 32px; color: #ff4d4f; font-weight: 700; margin: 16px 0; }
.orig { font-size: 16px; color: #bbb; text-decoration: line-through; margin-left: 10px; font-weight: 400; }
.stock { color: #666; margin-bottom: 12px; }
.qty-row { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; color: #666; }
.action-row { display: flex; gap: 12px; }
.desc-section { margin-top: 20px; border-radius: 12px; }
.desc-content { line-height: 1.8; }
.desc-content :deep(img) { max-width: 100%; }
.desc-content :deep(p) { margin: 8px 0; }
.review-section { margin-top: 20px; border-radius: 12px; }
.review-item { padding: 14px 0; border-bottom: 1px solid #f5f5f5; }
.review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.review-time { color: #ccc; font-size: 12px; }
</style>
