import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<any>(null)

  async function login(phone: string, password: string) {
    const res: any = await request.post('/user/login', { phone, password }, {
      params: { phone, password }
    })
    token.value = res.data
    localStorage.setItem('token', res.data)
    return res
  }

  async function register(phone: string, password: string, nickname: string) {
    const res: any = await request.post('/user/register', null, {
      params: { phone, password, nickname }
    })
    token.value = res.data
    localStorage.setItem('token', res.data)
    return res
  }

  async function fetchUser() {
    if (!token.value) return
    try {
      const res: any = await request.get('/user/info')
      user.value = res.data
    } catch { /* ignore */ }
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  return { token, user, login, register, fetchUser, logout }
})
