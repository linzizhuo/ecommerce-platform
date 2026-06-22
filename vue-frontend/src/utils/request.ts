import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器: 带token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截器: 统一错误处理
request.interceptors.response.use(
  res => {
    if (res.data.code === 401) {
      localStorage.removeItem('token')
      router.push('/login')
      ElMessage.error('请先登录')
      return Promise.reject(res.data)
    }
    return res.data
  },
  err => {
    ElMessage.error('网络错误')
    return Promise.reject(err)
  }
)

export default request
