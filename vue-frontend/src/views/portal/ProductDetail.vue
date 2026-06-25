<template>
  <div class="detail-page">
    <header class="top-bar"><div class="inner">
      <router-link to="/" class="logo">CloudMall</router-link>
      <router-link to="/cart"><el-icon><ShoppingCart /></el-icon></router-link>
    </div></header>
    <div class="content" v-if="product">
      <div class="detail-layout">
        <div class="main-img"><div class="img-placeholder">{{ product.name?.charAt(0) }}</div></div>
        <div class="info">
          <h2>{{ product.name }}</h2>
          <p class="brand">品牌: {{ product.brand }}</p>
          <el-divider />
          <h4>规格选择</h4>
          <el-radio-group v-model="selectedSkuId">
            <el-radio-button v-for="sku in skuList" :key="sku.id" :value="sku.id">
              {{ formatSpec(sku.specInfo) }} - ¥{{ (sku.price / 100).toFixed(2) }}
            </el-radio-button>
          </el-radio-group>
          <p v-if="selectedSku" class="price">¥{{ (selectedSku.price / 100).toFixed(2) }}
            <span class="orig">¥{{ (selectedSku.originalPrice / 100).toFixed(2) }}</span>
          </p>
          <p>库存: {{ selectedSku?.stock || 0 }}</p>
          <el-input-number v-model="quantity" :min="1" :max="selectedSku?.stock || 1" />
          <div style="margin-top:20px">
            <el-button type="danger" size="large" @click="addToCart">加入购物车</el-button>
            <el-button type="warning" size="large" @click="buyNow">立即购买</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getProductDetail } from '@/api/product'
import request from '@/utils/request'
import { useUserStore } from '@/stores/userStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const product = ref<any>(null)
const skuList = ref<any[]>([])
const selectedSkuId = ref<number | null>(null)
const quantity = ref(1)

const selectedSku = computed(() => skuList.value.find(s => s.id === selectedSkuId.value))

async function addToCart() {
  if (!userStore.token) { router.push('/login'); return }
  if (!selectedSku.value) { ElMessage.warning('请选择规格'); return }
  await request.post('/cart', {
    skuId: selectedSku.value.id,
    productName: product.value.name,
    specInfo: selectedSku.value.specInfo,
    price: selectedSku.value.price,
    quantity: quantity.value,
    image: ''
  })
  ElMessage.success('已加入购物车')
}

function buyNow() {
  addToCart().then(() => router.push('/cart'))
}

function formatSpec(spec: string): string {
  try { return Object.values(JSON.parse(spec)).join(' / ') } catch { return spec }
}

onMounted(async () => {
  const id = route.params.id
  const res: any = await getProductDetail(Number(id))
  product.value = res.data?.product || res.data
  skuList.value = res.data?.skuList || []
  if (skuList.value.length) selectedSkuId.value = skuList.value[0].id
})
</script>

<style scoped>
.detail-page { min-height: 100vh; background: #f5f5f5; }
.top-bar { background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 12px 20px; }
.top-bar .inner { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; gap: 20px; }
.logo { font-size: 24px; font-weight: bold; color: #ff4d4f; text-decoration: none; }
.content { max-width: 1200px; margin: 0 auto; padding: 20px; }
.detail-layout { display: flex; gap: 40px; background: #fff; padding: 30px; border-radius: 8px; margin-top: 20px; }
.main-img { width: 450px; }
.img-placeholder { width: 100%; height: 400px; background: linear-gradient(135deg, #667eea, #764ba2); display: flex; align-items: center; justify-content: center; font-size: 80px; color: #fff; border-radius: 8px; }
.info h2 { font-size: 22px; }
.price { font-size: 28px; color: #ff4d4f; margin: 15px 0; }
.orig { font-size: 14px; color: #999; text-decoration: line-through; margin-left: 10px; }
.brand { color: #999; }
</style>
