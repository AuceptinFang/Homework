import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  base: '/', // 修改为根路径，适合直接部署在服务器上
  server: {
    host: '0.0.0.0', // 允许外部访问
    port: 3000, // 修改为非特权端口
    proxy: {
      '/api': {
        target: 'http://134.175.229.249:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api'),
        secure: false,
        ws: true
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
