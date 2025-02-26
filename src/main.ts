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
      // 确保所有请求都有 /api 前缀
      const cleanPath = apiPath.startsWith('/api') ? apiPath : `/api${apiPath}`
      const originalUrl = `http://134.175.229.249:8080${cleanPath}`
      
      if (config.method?.toLowerCase() === 'get') {
        config.url = ''
        config.params = { url: originalUrl }
      } else {
        config.url = '/raw'
        config.params = { url: originalUrl }
        config.headers['Content-Type'] = 'application/json'
        config.data = JSON.stringify(config.data)
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
    console.log('原始响应数据:', response)

    // 处理 allorigins 代理的响应
    if (response.config.baseURL?.includes('allorigins.win')) {
      try {
        // 尝试解析响应数据
        const responseData = typeof response.data === 'string' 
          ? JSON.parse(response.data)
          : response.data
        console.log('处理后的响应数据:', responseData)
        return { ...response, data: responseData }
      } catch (error) {
        console.error('解析响应数据失败:', error)
        return response
      }
    }

    return response
  },
  error => {
    console.error('响应错误:', error)
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
