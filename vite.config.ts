import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  base: '/Homework/', // GitHub Pages的仓库名
  server: {
    host: '0.0.0.0', // 允许外部访问
    port: 3000, // 修改为非特权端口
    proxy: {
      '/api': {
        target: 'http://134.175.229.249:8080', // 云服务器地址
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api'), // 保持 /api 前缀
        secure: false, // 允许无效证书
        ws: true // 支持 websocket
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    // 生产环境移除 console
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    }
  }
})
