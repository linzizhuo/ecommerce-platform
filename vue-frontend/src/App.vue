<template>
  <router-view />
</template>

<script setup lang="ts">
import { watch, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { ElNotification } from 'element-plus'

const userStore = useUserStore()
let ws: WebSocket | null = null

function connect() {
  const token = localStorage.getItem('token')
  if (!token || ws) return // 已连接或无token则跳过

  const proto = location.protocol === 'https:' ? 'wss:' : 'ws:'
  ws = new WebSocket(`${proto}//${location.host}/ws/message?token=${token}`)

  ws.onmessage = (e) => {
    ElNotification({ title: '新消息', message: e.data, type: 'info', duration: 5000 })
  }
  ws.onclose = () => { ws = null }
  ws.onerror = () => { ws?.close() }
}

function disconnect() {
  ws?.close()
  ws = null
}

onMounted(() => connect())
onUnmounted(() => disconnect())

// 登录后自动连接
watch(() => userStore.token, (t) => { if (t) connect() })
// 退出后断开
watch(() => userStore.token, (t) => { if (!t) disconnect() })
</script>
