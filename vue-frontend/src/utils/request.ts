import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// ====== 请求去重 + 短时缓存 ======

interface CacheEntry {
  data: any
  timestamp: number
}

// 正在飞行中的请求 Map：key(URL+params) → Promise
const pendingMap = new Map<string, Promise<any>>()
// GET 请求结果缓存 Map：key(URL+params) → { data, timestamp }
const cacheMap = new Map<string, CacheEntry>()
const CACHE_TTL = 30_000 // GET 缓存 30 秒

/** 生成请求唯一 key */
function requestKey(config: AxiosRequestConfig): string {
  const { method, url, params } = config
  return `${method || 'GET'}:${url}:${JSON.stringify(params || {})}`
}

// ====== 请求拦截器 ======

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`

  // GET 请求：检查缓存
  if ((config.method || 'get').toLowerCase() === 'get') {
    const key = requestKey(config)
    const cached = cacheMap.get(key)
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      // 缓存命中 → 直接返回（通过 adapter 欺骗 axios）
      config.adapter = () => Promise.resolve({
        data: cached.data,
        status: 200, statusText: 'OK',
        headers: {}, config
      })
    }
  }

  return config
})

// ====== 响应拦截器 ======

request.interceptors.response.use(
  res => {
    // 非 GET 请求或 adapter 劫持的请求，不需要缓存
    if (res.config.method && res.config.method.toLowerCase() === 'get' && !res.config.adapter) {
      // 缓存 GET 结果
      const key = requestKey(res.config)
      cacheMap.set(key, { data: res.data, timestamp: Date.now() })
    }

    if (res.data.code === 401) {
      cacheMap.clear()
      pendingMap.clear()
      localStorage.removeItem('token')
      router.push('/login')
      return Promise.reject(res.data)
    }
    if (res.data.code !== 200) {
      ElMessage.error(res.data.message || '请求失败')
      return Promise.reject(res.data)
    }
    return res.data
  },
  err => {
    const msg = err.response?.data?.message || err.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default request
