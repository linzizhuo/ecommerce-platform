<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">👥 拼团</div>
      <router-link to="/" class="back">返回</router-link>
    </div></header>
    <div class="container" v-if="data">
      <el-card class="gb-card">
        <h2>拼团详情</h2>
        <p>状态: {{ ['拼团中','已成团','已失败'][data.group.status] }}</p>
        <p>拼团价: ¥{{ ((data.group.groupPrice||0)/100).toFixed(2) }}</p>
        <p>进度: {{ data.group.currentCount }}/{{ data.group.requiredCount }} 人</p>
        <el-progress :percentage="(data.group.currentCount/data.group.requiredCount*100)" :color="'#ff4d4f'" />
        <el-button v-if="data.group.status===0" type="danger" style="margin-top:16px" @click="join">我要参团</el-button>
      </el-card>
      <h3 style="margin-top:20px">成员列表</h3>
      <el-table :data="data.members">
        <el-table-column label="类型"><template #default="{row}">{{row.isLeader?'👑 团长':'团员'}}</template></el-table-column>
        <el-table-column prop="userId" label="用户ID" />
        <el-table-column prop="joinTime" label="加入时间" />
      </el-table>
    </div>
    <el-empty v-else description="拼团不存在" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { joinGroup, getGroupDetail } from '@/api/groupbuy'
import { ElMessage } from 'element-plus'
const route = useRoute()
const data = ref<any>(null)
async function load() { const r:any=await getGroupDetail(Number(route.params.id));data.value=r.data }
async function join() { await joinGroup(Number(route.params.id));ElMessage.success('参团成功');load() }
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
.container{max-width:800px;margin:0 auto;padding:20px}
.gb-card{border-radius:12px;text-align:center;padding:10px}
.gb-card h2{color:#ff4d4f}
</style>
