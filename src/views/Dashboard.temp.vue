<template>
  <div class="dashboard-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>作业提交系统</h2>
          <div class="user-info">
            <span>{{ username }}</span>
            <el-button type="text" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>我的作业</span>
                  <el-button type="primary" @click="$router.push('/submit')">
                    提交新作业
                  </el-button>
                </div>
              </template>
              
              <el-table :data="homeworkList" style="width: 100%">
                <el-table-column prop="title" label="作业标题" />
                <el-table-column prop="submitTime" label="提交时间" width="180" />
                <el-table-column prop="status" label="状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="getStatusType(row.status)">
                      {{ getStatusText(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="{ row }">
                    <el-button 
                      type="primary" 
                      link
                      @click="viewHomework(row)"
                    >
                      查看
                    </el-button>
                    <el-button 
                      v-if="row.status !== 'approved'"
                      type="danger" 
                      link
                      @click="deleteHomework(row)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '')

// 模拟作业列表数据
const homeworkList = ref([
  {
    id: 1,
    title: '第一次作业',
    submitTime: '2024-03-20 14:30',
    status: 'pending'
  },
  {
    id: 2,
    title: '第二次作业',
    submitTime: '2024-03-19 15:45',
    status: 'approved'
  }
])

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    pending: '待审核',
    approved: '已通过',
    rejected: '未通过'
  }
  return texts[status] || '未知'
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('isAdmin')
    localStorage.removeItem('username')
    router.push('/')
  })
}

const viewHomework = (homework: any) => {
  // TODO: 实现查看作业详情
  ElMessage.info('查看作业详情：' + homework.title)
}

const deleteHomework = (homework: any) => {
  ElMessageBox.confirm('确定要删除这个作业吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // TODO: 实现删除作业
    homeworkList.value = homeworkList.value.filter(item => item.id !== homework.id)
    ElMessage.success('删除成功')
  })
}
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.el-header {
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12);
  position: relative;
  z-index: 1;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-main {
  padding: 20px;
}
</style> 