<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">🎁 组合套餐</div>
      <router-link to="/" class="back">返回首页</router-link>
    </div></header>
    <div class="container">
      <div class="banner">🎁 超值套餐 · 一站购齐 · 立省更多</div>
      <div v-if="list.length" class="combo-grid">
        <el-card v-for="c in list" :key="c.id" shadow="hover" class="combo-card">
          <h3>{{ c.name }}</h3>
          <p class="cb-desc">{{ c.description }}</p>
          <p class="cb-price">
            <span class="cb-discount">¥{{ (c.discountPrice / 100).toFixed(2) }}</span>
            <span class="cb-original">¥{{ (c.totalPrice / 100).toFixed(2) }}</span>
            <el-tag type="danger" size="small">省{{ ((c.totalPrice - c.discountPrice) / 100).toFixed(2) }}</el-tag>
          </p>
          <div v-if="c.items" class="cb-items">
            <el-tag v-for="item in c.items" :key="item.id" size="small" style="margin:2px">SKU#{{ item.skuId }} x{{ item.quantity }}</el-tag>
          </div>
          <el-button type="primary" style="margin-top:12px" @click="addToCart(c)">加入购物车</el-button>
        </el-card>
      </div>
      <el-empty v-else description="暂无组合套餐" />
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getComboList } from '@/api/combo'
import { ElMessage } from 'element-plus'

const list = ref<any[]>([])
async function load() { const r: any = await getComboList(); list.value = r.data || [] }
function addToCart(c: any) { ElMessage.success('套餐「' + c.name + '」已加入购物车') }
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
.banner{background:linear-gradient(135deg,#722ed1,#b37feb);color:#fff;padding:30px;border-radius:12px;font-size:24px;font-weight:700;text-align:center;margin-bottom:20px}
.combo-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:18px}
.combo-card{border-radius:12px;text-align:center;padding:20px}
.cb-desc{color:#666;font-size:14px;margin:8px 0}
.cb-price{margin:8px 0}
.cb-discount{color:#ff4d4f;font-size:22px;font-weight:700}
.cb-original{color:#999;text-decoration:line-through;margin-left:8px;font-size:14px}
.cb-items{margin:8px 0}
</style>
