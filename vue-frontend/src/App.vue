<template>
  <router-view v-slot="{ Component, route }">
    <keep-alive :include="keepAlivePages">
      <component :is="Component" :key="route.path" />
    </keep-alive>
  </router-view>
</template>

<script setup lang="ts">
import { computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { ElNotification } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
let ws: WebSocket | null = null

/** 页面缓存白名单：这些页面的组件实例会被缓存，切换时不重新渲染 */
const keepAlivePages = computed(() => {
  return router.getRoutes()
    .filter(r => r.meta?.keepAlive)
    .map(r => r.name)
    .filter(Boolean) as string[]
})

function connect() {
  const token = localStorage.getItem('token')
  if (!token || ws) return

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

watch(() => userStore.token, (t) => { if (t) connect() })
watch(() => userStore.token, (t) => { if (!t) disconnect() })
</script>
