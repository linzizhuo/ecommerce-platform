<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">❤️ 我的收藏</div>
      <router-link to="/user/center" class="back">返回</router-link>
    </div></header>
    <div class="container">
      <div v-if="list.length" class="fav-grid">
        <el-card v-for="f in list" :key="f.id" shadow="hover" class="fav-card">
          <h4 @click="$router.push(`/product/${f.productId}`)">商品 #{{ f.productId }}</h4>
          <el-button size="small" type="danger" plain @click="remove(f.productId)">取消收藏</el-button>
        </el-card>
      </div>
      <el-empty v-else description="暂无收藏" />
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFavorites, removeFavorite } from '@/api/favorite'
import { ElMessage } from 'element-plus'
const list = ref<any[]>([])
async function load() { const r: any = await getFavorites(); list.value = r.data || [] }
async function remove(pid: number) { await removeFavorite(pid); ElMessage.success('已取消'); load() }
onMounted(load)
</script>
<style scoped>
.page{background:#f0f2f5;min-height:100vh}
.top-bar{background:#fff;box-shadow:0 1px 4px rgba(0,0,0,0.06);position:sticky;top:0;z-index:100}
.inner{max-width:1200px;margin:0 auto;padding:0 20px}
.top-row{display:flex;align-items:center;gap:24px;height:60px}
.logo{font-size:22px;font-weight:700;color:#ff4d4f;text-decoration:none}
.title{font-size:20px;font-weight:700;color:#333;flex:1}
.back{color:#999;text-decoration:none;font-size:14px}
.container{max-width:1200px;margin:0 auto;padding:20px}
.fav-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:18px}
.fav-card{border-radius:12px;text-align:center;padding:16px}
.fav-card h4{cursor:pointer;color:#333;margin-bottom:12px}
.fav-card h4:hover{color:#ff4d4f}
</style>
