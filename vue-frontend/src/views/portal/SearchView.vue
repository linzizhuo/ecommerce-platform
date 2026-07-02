<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索商品..." @input="onInput" @keyup.enter="doSearch" size="large">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      <router-link to="/" class="back">取消</router-link>
    </div></header>
    <div class="container">
      <div v-if="suggestions.length && keyword" class="suggest-box">
        <div v-for="s in suggestions" :key="s.keyword" class="suggest-item" @click="keyword=s.keyword;doSearch()">
          {{ s.keyword }} <span class="count">{{ s.count }}次</span>
        </div>
      </div>
      <div v-if="hot.length && !keyword" class="hot-section">
        <h3>🔥 热门搜索</h3>
        <el-tag v-for="h in hot" :key="h.keyword" class="hot-tag" @click="keyword=h.keyword;doSearch()">{{ h.keyword }}</el-tag>
      </div>
      <div v-if="results.length" class="result-list">
        <el-card v-for="p in results" :key="p.id" shadow="hover" @click="$router.push(`/product/${p.id}`)" class="result-card">
          <h3>{{ p.name }}</h3>
          <p class="brand">{{ p.brand }}</p>
        </el-card>
      </div>
      <el-empty v-if="searched && !results.length" description="未找到相关商品" />
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { suggest, search, hotWords } from '@/api/search'

const keyword = ref('')
const suggestions = ref<any[]>([])
const results = ref<any[]>([])
const hot = ref<any[]>([])
const searched = ref(false)
let timer: any = null

async function onInput() {
  if (!keyword.value) { suggestions.value = []; return }
  clearTimeout(timer)
  timer = setTimeout(async () => { const r: any = await suggest(keyword.value); suggestions.value = r.data || [] }, 300)
}
async function doSearch() { suggestions.value = []; const r: any = await search(keyword.value); results.value = r.data || []; searched.value = true }
async function loadHot() { const r: any = await hotWords(); hot.value = r.data || [] }
loadHot()
</script>
<style scoped>
.page{background:#f0f2f5;min-height:100vh}
.top-bar{background:#fff;box-shadow:0 1px 4px rgba(0,0,0,0.06);position:sticky;top:0;z-index:100}
.inner{max-width:1200px;margin:0 auto;padding:0 20px}
.top-row{display:flex;align-items:center;gap:16px;height:60px}
.logo{font-size:22px;font-weight:700;color:#ff4d4f;text-decoration:none}
.search-bar{flex:1}
.back{color:#999;text-decoration:none;font-size:14px}
.container{max-width:800px;margin:0 auto;padding:20px}
.suggest-box{background:#fff;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,0.1);overflow:hidden}
.suggest-item{padding:12px 16px;cursor:pointer;display:flex;justify-content:space-between;border-bottom:1px solid #f5f5f5}
.suggest-item:hover{background:#f0f2f5}
.count{color:#bbb;font-size:12px}
.hot-section{padding:20px 0}
.hot-section h3{margin-bottom:12px}
.hot-tag{margin:4px 8px 4px 0;cursor:pointer}
.result-card{margin-bottom:12px;cursor:pointer;border-radius:12px}
.brand{color:#999;font-size:13px;margin-top:4px}
</style>
