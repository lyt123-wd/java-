import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
})

// 请求拦截：自动携带token
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['token'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截：统一处理返回、401自动跳登录
service.interceptors.response.use(
  response => {
    const res = response.data
    // 401 未授权/登录过期
    if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('userId')
      router.push('/login')
      return Promise.reject(new Error(res.msg || '未授权'))
    }
    return res
  },
  error => {
    ElMessage.error(error.message || '网络请求失败')
    return Promise.reject(error)
  }
)

export default service
