<template>
  <div class="home">
    <header class="top-bar">
      <div class="inner top-row">
        <router-link to="/" class="logo">🛒 CloudMall</router-link>
        <div class="search-bar">
          <el-input v-model="keyword" placeholder="搜索你想要的..." @keyup.enter="search" size="large" class="search-input" />
        </div>
        <div class="user-nav">
          <template v-if="userStore.token">
            <router-link to="/cart"><el-badge :value="0" :hidden="true"><el-icon :size="22"><ShoppingCart /></el-icon></el-badge></router-link>
            <router-link to="/orders"><el-icon :size="20"><Tickets /></el-icon> 订单</router-link>
            <el-dropdown>
              <span class="user-tag">{{ userStore.user?.nickname || '用户' }} <el-icon><ArrowDown /></el-icon></span>
              <template #dropdown><el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/user/center')">个人中心</el-dropdown-item>
                <el-dropdown-item @click="userStore.logout()" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu></template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="btn-login">登录</router-link>
            <router-link to="/register" class="btn-register">注册</router-link>
          </template>
        </div>
      </div>
    </header>

    <nav class="cat-nav">
      <div class="inner">
        <el-button :type="!catId ? 'danger' : 'default'" round @click="catId = null; loadProducts()">🔥 全部</el-button>
        <template v-for="cat in topCats" :key="cat.id">
          <el-button :type="catId === cat.id ? 'danger' : 'default'" round @click="catId = cat.id; loadProducts()">{{ cat.name }}</el-button>
          <el-button v-for="sub in subCats(cat.id)" :key="sub.id" :type="catId === sub.id ? 'danger' : ''" size="small" round
            @click="catId = sub.id; loadProducts()" style="margin-right:4px">{{ sub.name }}</el-button>
        </template>
      </div>
    </nav>

    <div class="container">
      <!-- 搜索时显示关键词 -->
      <div v-if="keyword" class="search-hint">搜索结果: "{{ keyword }}"</div>

      <div class="product-grid">
        <el-card v-for="p in products" :key="p.id" shadow="hover" class="product-card"
          @click="$router.push(`/product/${p.id}`)">
          <div class="card-img" :style="{background: colors[p.id % colors.length]}">
            <span class="card-letter">{{ p.name?.charAt(0) || '商' }}</span>
            <span class="card-tag" v-if="p.id <= 3">热卖</span>
          </div>
          <div class="card-body">
            <h4>{{ p.name }}</h4>
            <p class="brand">{{ p.brand }} · {{ catName(p.categoryId) }}</p>
            <div class="card-footer">
              <span class="card-price">¥{{ ((p.id * 3999 + 100) / 100).toFixed(0) }}起</span>
              <span class="card-sold">已售 {{ p.id * 37 + 100 }}</span>
            </div>
          </div>
        </el-card>
      </div>
      <el-empty v-if="!products.length && !keyword" description="暂无商品，商家正在上架中..." />
    </div>

    <footer class="site-footer">
      <p>CloudMall 高并发电商系统 © 2026 | Vue3 + Spring Cloud Alibaba</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ShoppingCart, Tickets, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import request from '@/utils/request'

const userStore = useUserStore()
const keyword = ref('')
const catId = ref<number | null>(null)
const products = ref<any[]>([])
const categories = ref<any[]>([])

const colors = ['linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)',
  'linear-gradient(135deg, #96fbc4 0%, #f9f586 100%)']

const topCats = computed(() => categories.value.filter((c: any) => c.level === 1))
function subCats(parentId: number) { return categories.value.filter((c: any) => c.parentId === parentId) }
function catName(catId: number): string {
  const c = categories.value.find((c: any) => c.id === catId)
  return c ? c.name : ''
}

async function loadProducts() {
  // 获取所有相关类目ID(父类目+子类目)
  let catIds: number[] = []
  if (catId.value) {
    catIds = [catId.value]
    // 如果是父类目, 加入子类目
    const children = categories.value.filter((c: any) => c.parentId === catId.value).map((c: any) => c.id)
    catIds.push(...children)
  }
  const allProducts: any[] = []
  for (const cid of catIds.length ? catIds : [null]) {
    const res: any = await request.get('/product/list', { params: { keyword: keyword.value, categoryId: cid } })
    if (res.data) allProducts.push(...res.data)
  }
  // 去重
  const seen = new Set()
  products.value = allProducts.filter((p: any) => { const ok = !seen.has(p.id); seen.add(p.id); return ok })
}
async function loadCategories() {
  const res: any = await request.get('/product/category/list')
  categories.value = res.data || []
}
function search() { catId.value = null; loadProducts() }

onMounted(() => { loadCategories(); loadProducts(); userStore.fetchUser() })
</script>

<style scoped>
.home { background: #f0f2f5; min-height: 100vh; }
.top-bar { background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); position: sticky; top: 0; z-index: 100; }
.inner { max-width: 1200px; margin: 0 auto; padding: 0 20px; }
.top-row { display: flex; align-items: center; gap: 24px; height: 60px; }
.logo { font-size: 22px; font-weight: 700; color: #ff4d4f; text-decoration: none; letter-spacing: -1px; }
.search-bar { flex: 1; max-width: 480px; }
.user-nav { display: flex; gap: 18px; align-items: center; white-space: nowrap; }
.user-nav a { color: #555; text-decoration: none; font-size: 14px; display: flex; align-items: center; gap: 4px; }
.user-tag { cursor: pointer; color: #ff4d4f; font-weight: 500; }
.btn-login { color: #ff4d4f !important; border: 1px solid #ff4d4f; padding: 5px 16px; border-radius: 20px; }
.btn-register { background: #ff4d4f; color: #fff !important; padding: 6px 16px; border-radius: 20px; }
.cat-nav { background: #fff; padding: 8px 0; border-top: 1px solid #f5f5f5; }
.container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.search-hint { padding: 10px 0; color: #999; font-size: 14px; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; }
@media (max-width: 960px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 640px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
.product-card { border-radius: 12px; overflow: hidden; cursor: pointer; transition: all 0.25s; border: none; }
.product-card:hover { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(0,0,0,0.12); }
.card-img { height: 180px; display: flex; align-items: center; justify-content: center; position: relative; }
.card-letter { font-size: 56px; color: rgba(255,255,255,0.85); font-weight: bold; }
.card-tag { position: absolute; top: 10px; left: 10px; background: #ff4d4f; color: #fff; padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.card-body { padding: 14px; }
.card-body h4 { font-size: 14px; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.brand { color: #aaa; font-size: 12px; margin-bottom: 8px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.card-price { font-size: 18px; font-weight: 700; color: #ff4d4f; }
.card-sold { font-size: 12px; color: #bbb; }
.site-footer { text-align: center; padding: 40px 0 20px; color: #ccc; font-size: 13px; }
</style>
