<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">⭐ 我的积分</div>
      <router-link to="/user/center" class="back">返回</router-link>
    </div></header>
    <div class="container">
      <el-card class="point-card" v-if="point">
        <h2>{{ point.availablePoint || 0 }} <span>可用积分</span></h2>
        <p>累计获得: {{ point.totalPoint || 0 }}</p>
      </el-card>
      <div style="display:flex;gap:12px;margin:16px 0">
        <el-card v-for="l in levels" :key="l.id" class="level-card">
          <h4>{{ l.levelName }}</h4>
          <p>消费满 ¥{{ (l.minAmount/100).toFixed(0) }}</p>
          <p>折扣: {{ l.discountRate }}%</p>
        </el-card>
      </div>
      <el-table :data="logs" v-loading="loadingLogs">
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column prop="description" label="说明" />
        <el-table-column label="积分"><template #default="{row}"><span :style="{color:row.point>0?'#67c23a':'#f56c6c'}">{{row.point>0?'+':''}}{{row.point}}</span></template></el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyPoints, getPointLogs, getLevels } from '@/api/point'
const point = ref<any>(null)
const logs = ref<any[]>([])
const levels = ref<any[]>([])
const loadingLogs = ref(false)
async function load() {
  const [p,l,ls]:any[] = await Promise.all([getMyPoints(),getPointLogs(),getLevels()])
  point.value=p.data; logs.value=l.data||[]; levels.value=ls.data||[]
}
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
.container{max-width:900px;margin:0 auto;padding:20px}
.point-card{border-radius:12px;text-align:center;background:linear-gradient(135deg,#ff4d4f,#ff7875);color:#fff}
.point-card h2{font-size:36px;margin:0}
.point-card span{font-size:14px;opacity:0.9}
.level-card{border-radius:8px;flex:1;text-align:center}
.level-card h4{color:#ff4d4f}
</style>
