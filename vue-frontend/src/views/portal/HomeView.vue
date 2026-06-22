<template>
  <div class="home">
    <header class="top-bar">
      <div class="inner">
        <router-link to="/" class="logo">CloudMall</router-link>
        <div class="search-bar">
          <el-input v-model="keyword" placeholder="搜索商品..." @keyup.enter="search" size="large">
            <template #append><el-button @click="search" :icon="Search">搜索</el-button></template>
          </el-input>
        </div>
        <div class="user-nav">
          <template v-if="userStore.token">
            <router-link to="/cart"><el-icon><ShoppingCart /></el-icon> 购物车</router-link>
            <router-link to="/orders">我的订单</router-link>
            <router-link to="/user/center">{{ userStore.user?.nickname || '用户' }}</router-link>
            <a @click="userStore.logout()">退出</a>
          </template>
          <template v-else>
            <router-link to="/login">登录</router-link>
            <router-link to="/register">注册</router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- 类目导航 -->
    <nav class="cat-nav">
      <div class="inner">
        <el-button v-for="cat in categories" :key="cat.id" :type="catId === cat.id ? 'danger' : ''"
          @click="catId = cat.id; loadProducts()">{{ cat.name }}</el-button>
        <el-button :type="!catId ? 'danger' : ''" @click="catId = null; loadProducts()">全部</el-button>
      </div>
    </nav>

    <!-- 商品网格 -->
    <div class="inner product-area">
      <el-row :gutter="16">
        <el-col :span="6" v-for="p in products" :key="p.id">
          <el-card :body-style="{ padding: '0' }" shadow="hover" class="product-card"
            @click="$router.push(`/product/${p.id}`)">
            <div class="img-box">{{ p.name?.charAt(0) }}</div>
            <div class="info">
              <h4>{{ p.name }}</h4>
              <p class="brand">{{ p.brand }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!products.length" description="暂无商品" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search, ShoppingCart } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { getProductList, getCategories } from '@/api/product'

const userStore = useUserStore()
const keyword = ref('')
const catId = ref<number | null>(null)
const products = ref<any[]>([])
const categories = ref<any[]>([])

async function loadProducts() {
  const res: any = await getProductList({ keyword: keyword.value, categoryId: catId.value })
  products.value = res.data || []
}
async function loadCategories() {
  const res: any = await getCategories()
  categories.value = res.data || []
}

function search() { loadProducts() }

onMounted(() => {
  loadCategories()
  loadProducts()
  userStore.fetchUser()
})
</script>

<style scoped>
.home { background: #f5f5f5; min-height: 100vh; }
.top-bar { background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.inner { max-width: 1200px; margin: 0 auto; padding: 12px 20px; display: flex; align-items: center; gap: 20px; }
.logo { font-size: 24px; font-weight: bold; color: #ff4d4f; text-decoration: none; }
.search-bar { flex: 1; max-width: 500px; }
.user-nav { display: flex; gap: 15px; white-space: nowrap; }
.user-nav a { color: #666; text-decoration: none; cursor: pointer; }
.cat-nav { background: #fff; padding: 10px 0; }
.product-area { padding: 20px; }
.product-card { cursor: pointer; transition: transform 0.2s; margin-bottom: 16px; }
.product-card:hover { transform: translateY(-3px); }
.img-box { height: 180px; background: linear-gradient(135deg, #667eea, #764ba2); display: flex; align-items: center; justify-content: center; font-size: 48px; color: #fff; }
.info { padding: 12px; }
.info h4 { font-size: 14px; margin-bottom: 5px; }
.brand { color: #999; font-size: 12px; }
</style>
