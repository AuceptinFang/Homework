<template>
  <div class="offline-container">
    <el-card class="offline-card">
      <template #header>
        <div class="offline-header">
          <el-icon class="offline-icon"><WarningFilled /></el-icon>
          <h2>服务器连接失败</h2>
        </div>
      </template>
      
      <div class="offline-content">
        <p>无法连接到服务器，请检查您的网络连接或稍后再试。</p>
        <p>您可以尝试以下操作：</p>
        <ul>
          <li>检查您的网络连接</li>
          <li>确认服务器是否正在运行</li>
          <li>联系系统管理员</li>
        </ul>
        
        <div class="offline-actions">
          <el-button type="primary" @click="checkServerStatus">
            重试连接
          </el-button>
          <el-button @click="goToLogin">
            返回登录页
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { WarningFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { setServerOffline } from '../router'

const router = useRouter()

const checkServerStatus = async () => {
  try {
    await axios.get('/health', { timeout: 5000 })
    ElMessage.success('服务器连接恢复')
    setServerOffline(false)
    router.push('/')
  } catch (error) {
    ElMessage.error('服务器仍然无法连接')
    setServerOffline(true)
  }
}

const goToLogin = () => {
  router.push('/')
}
</script>

<style scoped>
.offline-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.offline-card {
  width: 500px;
  max-width: 90%;
}

.offline-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.offline-icon {
  font-size: 24px;
  color: #f56c6c;
}

.offline-content {
  padding: 20px 0;
}

.offline-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style> 