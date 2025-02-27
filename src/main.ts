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

    // 为 proxy.cors.sh 添加必要的头部
    if (config.baseURL?.includes('proxy.cors.sh')) {
      config.headers['X-Requested-With'] = 'XMLHttpRequest'
      
      // 处理 proxy.cors.sh 代理请求的路径问题
      const apiPath = config.url || ''
      
      // 移除开头的 /api 前缀，因为 baseURL 已经包含了 /api
      if (apiPath.startsWith('/api/')) {
        config.url = apiPath.substring(4) // 移除 '/api'
        console.log('移除重复的 /api 前缀，新路径:', config.url)
      } else if (apiPath.startsWith('/api')) {
        config.url = apiPath.substring(4) // 移除 '/api'
        console.log('移除重复的 /api 前缀，新路径:', config.url)
      }
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
        let responseData = response.data;
        
        // 如果是字符串（text/plain），尝试解析为 JSON
        if (typeof responseData === 'string') {
          try {
            responseData = JSON.parse(responseData);
            console.log('成功将 text/plain 解析为 JSON:', responseData);
          } catch (parseError) {
            console.error('解析 text/plain 为 JSON 失败:', parseError);
          }
        }
        console.log('处理后的响应数据:', responseData)
        return { ...response, data: responseData }
      } catch (error) {
        console.error('解析响应数据失败:', error)
        return response
      }
    }
    
    // 处理 proxy.cors.sh 代理的响应
    if (response.config.baseURL?.includes('proxy.cors.sh')) {
      try {
        // proxy.cors.sh 通常会保留原始响应的 Content-Type
        // 但我们仍然检查一下，以防万一
        let responseData = response.data;
        
        if (typeof responseData === 'string' && response.headers['content-type']?.includes('text/plain')) {
          try {
            responseData = JSON.parse(responseData);
            console.log('成功将 proxy.cors.sh 响应解析为 JSON:', responseData);
          } catch (parseError) {
            console.error('解析 proxy.cors.sh 响应失败:', parseError);
          }
        }
        
        console.log('处理后的 proxy.cors.sh 响应:', responseData);
        return { ...response, data: responseData };
      } catch (error) {
        console.error('处理 proxy.cors.sh 响应失败:', error);
        return response;
      }
    }

    return response
  },
  error => {
    console.error('错误:', error)
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
