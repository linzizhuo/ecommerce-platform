<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">🔔 消息中心</div>
      <el-button size="small" @click="readAll">全部已读</el-button>
      <router-link to="/user/center" class="back">返回</router-link>
    </div></header>
    <div class="container">
      <el-tabs v-model="tab">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="未读" name="unread" />
        <el-tab-pane label="系统" name="1" />
        <el-tab-pane label="订单" name="2" />
      </el-tabs>
      <div v-if="filtered.length" class="msg-list">
        <div v-for="m in filtered" :key="m.id" :class="['msg-item', { unread: !m.isRead }]" @click="readMsg(m)">
          <h4>{{ m.title }} {{ !m.isRead ? '🔴' : '' }}</h4>
          <p>{{ m.content }}</p>
          <span class="time">{{ m.createTime }}</span>
        </div>
      </div>
      <el-empty v-else description="暂无消息" />
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMessages, markRead, markAllRead } from '@/api/message'
import { ElMessage } from 'element-plus'
const tab = ref('all')
const list = ref<any[]>([])
const filtered = computed(() => {
  if (tab.value === 'all') return list.value
  if (tab.value === 'unread') return list.value.filter((m:any)=>!m.isRead)
  return list.value.filter((m:any)=>m.type===parseInt(tab.value))
})
async function load() { const r: any = await getMessages(); list.value = r.data || [] }
async function readMsg(m: any) { if(!m.isRead) { await markRead(m.id); m.isRead=1 } }
async function readAll() { await markAllRead(); ElMessage.success('全部已读'); load() }
onMounted(load)
</script>
<style scoped>
.page{background:#f0f2f5;min-height:100vh}
.top-bar{background:#fff;box-shadow:0 1px 4px rgba(0,0,0,0.06);position:sticky;top:0;z-index:100}
.inner{max-width:1200px;margin:0 auto;padding:0 20px}
.top-row{display:flex;align-items:center;gap:16px;height:60px}
.logo{font-size:22px;font-weight:700;color:#ff4d4f;text-decoration:none}
.title{font-size:20px;font-weight:700;color:#333;flex:1}
.back{color:#999;text-decoration:none;font-size:14px}
.container{max-width:800px;margin:0 auto;padding:20px}
.msg-list{background:#fff;border-radius:12px;overflow:hidden}
.msg-item{padding:16px 20px;border-bottom:1px solid #f5f5f5;cursor:pointer}
.msg-item:hover{background:#f9f9f9}
.msg-item.unread{background:#fff7f7}
.msg-item h4{font-size:15px;margin-bottom:4px}
.msg-item p{color:#999;font-size:13px;margin-bottom:4px}
.time{color:#ccc;font-size:12px}
</style>
