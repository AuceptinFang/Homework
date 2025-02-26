import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import axios from 'axios'

// 配置axios默认值
axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL
axios.defaults.headers.common['Content-Type'] = 'application/json'
axios.defaults.timeout = 10000 // 10秒超时

// 添加请求拦截器
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 处理 allorigins 代理请求
    if (config.baseURL?.includes('allorigins.win')) {
      const apiPath = config.url || ''
      const originalUrl = `http://134.175.229.249:8080/api${apiPath}`
      
      // 对于 GET 请求
      if (config.method?.toLowerCase() === 'get') {
        config.url = ''  // 清空 url，因为完整 url 将作为参数传递
        config.params = { url: originalUrl }
      } 
      // 对于 POST/PUT/DELETE 请求
      else {
        config.url = '/post'  // allorigins 的 POST 端点
        config.params = { url: originalUrl }
        config.headers['Content-Type'] = 'application/json'
        // 保存原始数据
        const originalData = config.data
        // 重构请求数据
        config.data = {
          method: config.method,
          body: JSON.stringify(originalData),
          headers: {
            'Content-Type': 'application/json'
          }
        }
      }
    }

    // 打印请求信息用于调试
    console.log('发送请求:', {
      baseURL: config.baseURL,
      url: config.url,
      method: config.method,
      headers: config.headers,
      data: config.data,
      params: config.params
    })

    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 添加响应拦截器
axios.interceptors.response.use(
  response => {
    console.log('响应成功:', {
      url: response.config.url,
      status: response.status,
      data: response.data
    })
    return response
  },
  error => {
    console.error('响应错误:', {
      url: error.config?.url,
      status: error.response?.status,
      data: error.response?.data
    })
    
    if (error.code === 'ECONNABORTED') {
      // 请求超时
      console.error('请求超时，服务器可能不可用')
    } else if (!error.response) {
      // 网络错误或服务器未响应
      console.error('无法连接到服务器，请检查网络连接或服务器状态')
    } else if (error.response?.status === 401 || error.response?.status === 403) {
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('isAdmin')
      localStorage.removeItem('username')
      // 跳转到登录页
      router.push('/')
    }
    
    return Promise.reject(error)
  }
)

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
