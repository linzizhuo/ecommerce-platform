import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<any>(null)
  const lastFetchTime = ref(0) // 上次拉取用户信息的时间戳

  async function login(phone: string, password: string) {
    const res: any = await request.post('/user/login', { phone, password })
    token.value = res.data
    localStorage.setItem('token', res.data)
    return res
  }

  async function register(phone: string, password: string, nickname: string) {
    const res: any = await request.post('/user/register', { phone, password, nickname })
    token.value = res.data
    localStorage.setItem('token', res.data)
    return res
  }

  async function fetchUser() {
    if (!token.value) return
    // 缓存策略：60秒内不重复拉取用户信息
    if (user.value && Date.now() - lastFetchTime.value < 60000) return
    try {
      const res: any = await request.get('/user/info')
      user.value = res.data
      lastFetchTime.value = Date.now()
    } catch { /* ignore */ }
  }

  function logout() {
    token.value = ''
    user.value = null
    lastFetchTime.value = 0
    localStorage.removeItem('token')
  }

  return { token, user, lastFetchTime, login, register, fetchUser, logout }
}, {
  // Pinia 持久化：token 和 user 自动同步 localStorage，刷新不丢失
  persist: {
    key: 'cloudmall-user',
    pick: ['token', 'user']
  }
})
