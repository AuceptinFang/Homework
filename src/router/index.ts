import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'

// 定义路由
const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/submit',
    name: 'SubmitHomework',
    component: () => import('../views/SubmitHomework.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/offline',
    name: 'Offline',
    component: () => import('../views/Offline.vue')
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 全局变量，记录服务器状态
let isServerOffline = false

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const isAdmin = localStorage.getItem('isAdmin') === 'true'

  // 如果目标是离线页面，直接允许
  if (to.name === 'Offline') {
    return next()
  }

  // 如果服务器离线且不是登录页，重定向到离线页面
  if (isServerOffline && to.name !== 'Login') {
    ElMessage.warning('服务器连接失败，部分功能可能不可用')
    return next({ name: 'Login' })
  }

  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login' })
  } else if (to.meta.requiresAdmin && !isAdmin) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

// 导出服务器状态设置函数，供其他组件使用
export const setServerOffline = (offline: boolean) => {
  isServerOffline = offline
}

export default router 