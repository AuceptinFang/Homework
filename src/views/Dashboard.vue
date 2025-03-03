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
        <!-- 服务器状态提示 -->
        <el-alert
          v-if="serverStatus === 'offline'"
          title="服务器连接失败，数据可能不是最新的"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />
        
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>
        
        <!-- 主要内容 -->
        <div v-else>
          <el-row :gutter="20">
            <!-- 可提交的作业列表 -->
            <el-col :span="24" style="margin-bottom: 20px">
              <el-card>
                <template #header>
                  <div class="card-header">
                    <span>可提交的作业</span>
                  </div>
                </template>
                
                <el-table :data="availableAssignments" style="width: 100%">
                  <el-table-column prop="title" label="作业标题" />
                  <el-table-column prop="description" label="作业描述" />
                  <el-table-column prop="deadline" label="截止时间" />
                  <el-table-column label="操作">
                    <template #default="{ row }">
                      <el-button type="primary" @click="submitHomework(row)">提交作业</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>

            <!-- 已提交的作业列表 -->
            <el-col :span="24">
              <el-card>
                <template #header>
                  <div class="card-header">
                    <span>提交记录</span>
                  </div>
                </template>
                
                <el-table :data="submissionList" style="width: 100%">
                  <el-table-column prop="title" label="作业标题" />
                  <el-table-column prop="submitTime" label="提交时间" />
                  <el-table-column prop="originalFilename" label="文件名">
                    <template #default="{ row }">
                      {{ row.originalFilename || '未知文件名' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态">
                    <template #default="{ row }">
                      <el-tag :type="row.status === 'submitted' ? 'success' : 'info'">
                        {{ row.status === 'submitted' ? '已提交' : '未提交' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作">
                    <template #default="{ row }">
                      <el-button type="primary" @click="viewHomework(row)">查看</el-button>
                      <el-button type="warning" @click="resubmitHomework(row)">重新提交</el-button>
                      <el-button type="danger" @click="deleteHomework(row)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>

    <!-- 提交作业对话框 -->
    <el-dialog
      v-model="submitDialogVisible"
      :title="currentAssignment?.title"
      width="50%"
    >
      <el-form
        ref="submitFormRef"
        :model="submitForm"
        label-width="120px"
      >
        <el-alert
          v-if="currentAssignment?.fileFormat === 'pdf'"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 15px"
        >
          <p>此作业要求提交PDF格式文件。您可以：</p>
          <ul>
            <li>直接上传PDF文件</li>
            <li>上传多张图片，系统将自动合并为PDF</li>
          </ul>
        </el-alert>
        
        <el-alert
          v-else
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 15px"
        >
          <p>此作业允许提交任意格式文件，无需转换为PDF。</p>
        </el-alert>
        
        <el-form-item label="提交类型" prop="submitType">
          <el-radio-group v-model="submitForm.submitType">
            <template v-if="currentAssignment?.fileFormat === 'pdf'">
              <el-radio label="pdf">PDF文件</el-radio>
              <el-radio label="images">多张图片</el-radio>
            </template>
            <template v-else>
              <el-radio label="pdf">PDF文件</el-radio>
              <el-radio label="images">图片文件</el-radio>
              <el-radio label="other">其他格式</el-radio>
            </template>
          </el-radio-group>
        </el-form-item>
        
        <!-- PDF文件上传 -->
        <el-form-item
          v-if="submitForm.submitType === 'pdf'"
          label="PDF文件"
          prop="file"
          :rules="[
            { required: submitForm.submitType === 'pdf', message: '请上传PDF文件' }
          ]"
        >
          <el-upload
            class="upload-container"
            :auto-upload="false"
            :limit="1"
            accept=".pdf"
            :on-change="handleFileChange"
          >
            <template #trigger>
              <el-button type="primary" class="upload-button">选择文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                只能上传 PDF 文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <!-- 多图片上传 -->
        <el-form-item
          v-if="submitForm.submitType === 'images'"
          label="图片文件"
          prop="imageFiles"
          :rules="[
            { required: submitForm.submitType === 'images', message: '请上传至少一张图片' }
          ]"
        >
          <el-upload
            class="upload-container"
            :auto-upload="false"
            :limit="10"
            multiple
            accept="image/*"
            list-type="picture-card"
            :on-change="handleImagesChange"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                {{ currentAssignment?.fileFormat === 'pdf' ? '可上传多张图片，将自动合并为PDF（最多10张）' : '可上传多张图片（最多10张）' }}
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <!-- 其他格式文件上传 -->
        <el-form-item
          v-if="submitForm.submitType === 'other'"
          label="其他文件"
          prop="otherFile"
          :rules="[
            { required: submitForm.submitType === 'other', message: '请上传文件' }
          ]"
        >
          <el-upload
            class="upload-container"
            :auto-upload="false"
            :limit="1"
            :on-change="handleOtherFileChange"
          >
            <template #trigger>
              <el-button type="primary" class="upload-button">选择文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                文件大小不超过20MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="备注" prop="description">
          <el-input
            v-model="submitForm.description"
            type="textarea"
            placeholder="请输入备注信息（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="submitDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { HomeworkAssignment, HomeworkSubmission } from '../types/homework'
import axios from 'axios'
import { setServerOffline } from '../router'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '')
const submitDialogVisible = ref(false)
const currentAssignment = ref<HomeworkAssignment | null>(null)
const submitFormRef = ref<FormInstance>()

// 可提交的作业列表
const availableAssignments = ref<HomeworkAssignment[]>([])
// 提交记录列表
const submissionList = ref<HomeworkSubmission[]>([])

// 服务器状态和加载状态
const serverStatus = ref<'checking' | 'online' | 'offline'>('checking')
const loading = ref(true)

// 获取可提交的作业列表
// const fetchAvailableAssignments = async () => {
//   try {
//     const response = await axios.get('/api/assignments', {
//       params: { onlyAvailable: true }
//     })
//     availableAssignments.value = response.data
//   } catch (error: any) {
//     ElMessage.error(error.response?.data?.error || '获取作业列表失败')
//   }
// }

// 获取提交记录
const fetchSubmissions = async () => {
  try {
    console.log('开始获取提交记录...')
    const response = await axios.get('/submissions/user')
    console.log('获取提交记录成功:', response.data)
    submissionList.value = response.data
  } catch (error: any) {
    console.error('获取提交记录失败:', error)
    console.error('错误详情:', error.response?.data)
    console.error('错误状态:', error.response?.status)
    ElMessage.error(error.response?.data?.error || '获取提交记录失败')
  }
}

// 检查服务器状态
const checkServerStatus = async () => {
  try {
    await axios.get('/health', { timeout: 5000 })
    serverStatus.value = 'online'
    setServerOffline(false)
    return true
  } catch (error) {
    console.error('服务器连接检查失败:', error)
    serverStatus.value = 'offline'
    setServerOffline(true)
    return false
  }
}

// 获取作业列表，添加错误处理
const fetchAssignments = async () => {
  loading.value = true
  try {
    // 先检查服务器状态
    const isOnline = await checkServerStatus()
    if (!isOnline) {
      // 如果离线，尝试从本地存储加载缓存数据
      const cachedData = localStorage.getItem('cachedAssignments')
      if (cachedData) {
        availableAssignments.value = JSON.parse(cachedData)
        ElMessage.warning('显示的是缓存数据，可能不是最新的')
      }
      loading.value = false
      return
    }
    
    // 如果在线，正常获取数据
    const response = await axios.get('/assignments')
    availableAssignments.value = response.data
    
    // 缓存数据到本地存储
    localStorage.setItem('cachedAssignments', JSON.stringify(response.data))
  } catch (error: any) {
    if (!error.response) {
      serverStatus.value = 'offline'
      setServerOffline(true)
      // 尝试从本地存储加载缓存数据
      const cachedData = localStorage.getItem('cachedAssignments')
      if (cachedData) {
        availableAssignments.value = JSON.parse(cachedData)
        ElMessage.warning('显示的是缓存数据，可能不是最新的')
      }
    } else {
      ElMessage.error('获取作业列表失败')
    }
  } finally {
    loading.value = false
  }
}

// 在组件挂载时获取数据
onMounted(() => {
  fetchAssignments()
  fetchSubmissions()
})

const submitForm = reactive({
  file: null as File | null,
  imageFiles: [] as File[],
  otherFile: null as File | null,
  description: '',
  submitType: 'pdf' // 默认为PDF上传
})

// 删除未使用的函数
// const getStatusType = (status: string) => {
//   const types: Record<string, string> = {
//     pending: 'warning',
//     approved: 'success',
//     rejected: 'danger'
//   }
//   return types[status] || 'info'
// }

// 删除未使用的函数
// const getStatusText = (status: string) => {
//   const texts: Record<string, string> = {
//     pending: '待审核',
//     approved: '已通过',
//     rejected: '未通过'
//   }
//   return texts[status] || '未知'
// }

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

const submitHomework = (assignment: HomeworkAssignment) => {
  currentAssignment.value = assignment
  submitForm.file = null
  submitForm.imageFiles = []
  submitForm.otherFile = null
  submitForm.description = ''
  submitForm.submitType = 'pdf'
  submitDialogVisible.value = true
}

const resubmitHomework = (submission: HomeworkSubmission) => {
  const assignment = availableAssignments.value.find(a => a.id === submission.assignmentId)
  if (assignment) {
    ElMessageBox.confirm('重新提交将会删除您之前提交的文件，确定要继续吗？', '确认重新提交', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      console.log('用户确认重新提交作业:', assignment.title)
      submitHomework(assignment)
    }).catch(() => {
      console.log('用户取消重新提交')
      ElMessage.info('已取消重新提交')
    })
  } else {
    ElMessage.warning('该作业已不允许提交')
  }
}

const handleFileChange = (uploadFile: any) => {
  if (uploadFile.raw) {
    if (uploadFile.raw.type !== 'application/pdf') {
      ElMessage.error('只能上传PDF文件！')
      return false
    }
    submitForm.file = uploadFile.raw
  }
}

const handleImagesChange = (uploadFile: any, uploadFiles: any[]) => {
  // 检查文件类型
  if (uploadFile.raw && !uploadFile.raw.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  
  // 更新图片文件列表
  submitForm.imageFiles = uploadFiles.map(file => file.raw).filter(Boolean)
}

const handleOtherFileChange = (uploadFile: any) => {
  if (uploadFile.raw) {
    // 检查文件大小
    if (uploadFile.raw.size > 20 * 1024 * 1024) {
      ElMessage.error('文件大小不能超过20MB！')
      return false
    }
    submitForm.otherFile = uploadFile.raw
  }
}

const handleSubmit = async () => {
  if (!submitFormRef.value) {
    console.error('表单引用不存在')
    return
  }
  
  if (!currentAssignment.value) {
    console.error('当前作业不存在')
    ElMessage.error('当前作业不存在')
    return
  }
  
  if (submitForm.submitType === 'pdf' && !submitForm.file) {
    console.error('未选择PDF文件')
    ElMessage.error('请选择要上传的PDF文件')
    return
  }
  
  if (submitForm.submitType === 'images' && submitForm.imageFiles.length === 0) {
    console.error('未选择图片文件')
    ElMessage.error('请选择要上传的图片文件')
    return
  }
  
  if (submitForm.submitType === 'other' && !submitForm.otherFile) {
    console.error('未选择文件')
    ElMessage.error('请选择要上传的文件')
    return
  }
  
  await submitFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        console.log('开始提交作业...')
        
        // 确保currentAssignment.value不为null
        if (!currentAssignment.value) {
          return
        }
        
        const assignmentId = currentAssignment.value.id
        const assignmentTitle = currentAssignment.value.title
        const isPdfOnly = currentAssignment.value.fileFormat === 'pdf'
        
        console.log('作业ID:', assignmentId)
        console.log('作业标题:', assignmentTitle)
        console.log('是否仅PDF:', isPdfOnly)
        console.log('提交类型:', submitForm.submitType)
        
        const formData = new FormData()
        
        if (submitForm.submitType === 'pdf') {
          // PDF文件上传
          const file = submitForm.file
          if (!file) return
          
          console.log('文件名:', file.name)
          console.log('文件大小:', file.size)
          console.log('文件类型:', file.type)
          
          // 再次验证文件类型
          if (file.type !== 'application/pdf') {
            console.error('文件类型错误:', file.type)
            ElMessage.error('只能上传PDF文件')
            return
          }
          
          // 验证文件大小
          if (file.size > 20 * 1024 * 1024) {
            console.error('文件过大:', file.size)
            ElMessage.error('文件大小不能超过20MB')
            return
          }
          
          formData.append('file', file)
          if (submitForm.description) {
            formData.append('description', submitForm.description)
          }
          
          console.log('发送PDF上传请求...')
          
          const response = await axios.post(
            `/submissions/assignment/${assignmentId}`,
            formData,
            {
              headers: {
                'Content-Type': 'multipart/form-data'
              },
              timeout: 30000 // 30秒超时
            }
          )
          
          console.log('提交作业成功:', response.data)
          submissionList.value.unshift(response.data)
          
        } else if (submitForm.submitType === 'images') {
          // 多图片上传
          const imageFiles = submitForm.imageFiles
          
          console.log('图片数量:', imageFiles.length)
          
          // 验证文件大小
          const totalSize = imageFiles.reduce((sum, file) => sum + file.size, 0)
          if (totalSize > 20 * 1024 * 1024) {
            console.error('文件总大小过大:', totalSize)
            ElMessage.error('文件总大小不能超过20MB')
            return
          }
          
          // 添加所有图片文件
          imageFiles.forEach(file => {
            formData.append('files', file)
          })
          
          if (submitForm.description) {
            formData.append('description', submitForm.description)
          }
          
          console.log('发送图片上传请求...')
          
          // 根据作业要求决定是否需要转换为PDF
          let endpoint = isPdfOnly 
            ? `/submissions/assignment/${assignmentId}/images` 
            : `/submissions/assignment/${assignmentId}/raw-images`
          
          console.log('使用端点:', endpoint)
          
          const response = await axios.post(
            endpoint,
            formData,
            {
              headers: {
                'Content-Type': 'multipart/form-data'
              },
              timeout: isPdfOnly ? 60000 : 30000 // 转PDF需要更长时间
            }
          )
          
          console.log('提交图片成功:', response.data)
          submissionList.value.unshift(response.data)
        } else if (submitForm.submitType === 'other') {
          // 其他格式文件上传
          const file = submitForm.otherFile
          if (!file) return
          
          console.log('文件名:', file.name)
          console.log('文件大小:', file.size)
          console.log('文件类型:', file.type)
          
          // 验证文件大小
          if (file.size > 20 * 1024 * 1024) {
            console.error('文件过大:', file.size)
            ElMessage.error('文件大小不能超过20MB')
            return
          }
          
          formData.append('file', file)
          if (submitForm.description) {
            formData.append('description', submitForm.description)
          }
          
          console.log('发送其他格式文件上传请求...')
          
          const response = await axios.post(
            `/submissions/assignment/${assignmentId}/other`,
            formData,
            {
              headers: {
                'Content-Type': 'multipart/form-data'
              },
              timeout: 30000 // 30秒超时
            }
          )
          
          console.log('提交文件成功:', response.data)
          submissionList.value.unshift(response.data)
        }
        
        ElMessage.success('作业提交成功！')
        submitDialogVisible.value = false
        
        // 刷新作业列表和提交记录
        fetchAssignments()
        fetchSubmissions()
      } catch (error: any) {
        console.error('提交作业失败:', error)
        
        if (error.response) {
          console.error('错误状态:', error.response.status)
          console.error('错误数据:', error.response.data)
          
          const errorMessage = error.response.data?.error || '提交作业失败'
          ElMessage.error(errorMessage)
        } else if (error.request) {
          console.error('请求未收到响应:', error.request)
          ElMessage.error('服务器未响应，请检查网络连接')
        } else {
          console.error('请求配置错误:', error.message)
          ElMessage.error(`请求错误: ${error.message}`)
        }
      }
    } else {
      console.error('表单验证失败')
      ElMessage.error('请填写完整的表单')
    }
  })
}

const viewHomework = async (submission: HomeworkSubmission) => {
  try {
    console.log('查看提交的作业:', submission);
    console.log('文件ID:', submission.id);
    
    // 先获取提交记录详情
    console.log('获取提交记录详情...');
    const detailResponse = await axios.get(`/submissions/${submission.id}`);
    const submissionDetail = detailResponse.data;
    console.log('提交记录详情:', submissionDetail);
    
    // 获取原始文件名
    let originalFilename = submissionDetail.originalFilename || `作业-${submission.id}.pdf`;
    
    // 如果原始文件名不存在，使用备用命名方案
    if (!originalFilename || originalFilename.trim() === '') {
      originalFilename = `作业-${submission.id}.pdf`;
    }
    
    console.log('原始文件名:', originalFilename);
    console.log('文件路径:', submissionDetail.filePath || '');
    
    // 显示加载提示
    const loadingInstance = ElMessage.info({
      message: '正在下载文件...',
      duration: 0
    });
    
    console.log('开始下载文件...');
    console.log('请求URL:', `/submissions/${submission.id}/file`);
    
    // 使用fetch API获取文件
    const response = await fetch(`/submissions/${submission.id}/file`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    
    if (!response.ok) {
      throw new Error(`下载失败: ${response.status} ${response.statusText}`);
    }
    
    // 获取文件内容
    const blob = await response.blob();
    console.log('文件下载成功，大小:', blob.size);
    
    // 关闭加载提示
    loadingInstance.close();
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = originalFilename; // 设置下载文件名
    console.log('设置下载文件名:', originalFilename);
    
    // 触发下载
    document.body.appendChild(link);
    link.click();
    
    // 清理
    window.URL.revokeObjectURL(url);
    document.body.removeChild(link);
    
    ElMessage.success('文件下载成功');
  } catch (error: any) {
    console.error('查看作业失败:', error);
    
    if (error.response) {
      console.error('错误状态:', error.response.status);
      console.error('错误数据:', error.response.data);
    }
    
    ElMessage.error(error.response?.data?.error || error.message || '下载文件失败');
  }
}

const deleteHomework = async (submission: HomeworkSubmission) => {
  ElMessageBox.confirm('确定要删除这个作业吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.delete(`/submissions/${submission.id}`)
      submissionList.value = submissionList.value.filter(item => item.id !== submission.id)
      ElMessage.success('删除成功')
    } catch (error: any) {
      ElMessage.error(error.response?.data?.error || '删除作业失败')
    }
  })
}
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  
  @media screen and (max-width: 768px) {
    padding: 0;
  }
}

.el-header {
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12);
  position: relative;
  z-index: 1;
  
  @media screen and (max-width: 768px) {
    padding: 0 1rem;
  }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  
  h2 {
    @media screen and (max-width: 768px) {
      font-size: 1.2rem;
    }
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  
  @media screen and (max-width: 768px) {
    gap: 8px;
    
    span {
      font-size: 0.9rem;
    }
  }
}

.el-main {
  padding: 20px;
  
  @media screen and (max-width: 768px) {
    padding: 10px;
  }
}

.el-card {
  @media screen and (max-width: 768px) {
    margin-bottom: 1rem;
  }
}

.el-table {
  @media screen and (max-width: 768px) {
    font-size: 0.9rem;
    
    .el-button {
      padding: 0.5rem;
      font-size: 0.9rem;
    }
  }
}

.el-dialog {
  @media screen and (max-width: 768px) {
    width: 90% !important;
    margin: 0 auto;
    
    .el-dialog__body {
      padding: 1rem;
    }
  }
}

.upload-container {
  .upload-button {
    width: 100%;
    padding: 1rem;
    border: 2px dashed #ddd;
    border-radius: 8px;
    text-align: center;
    
    @media screen and (max-width: 768px) {
      padding: 0.75rem;
    }
  }
  
  .file-list {
    margin-top: 1rem;
    
    .file-item {
      display: flex;
      align-items: center;
      padding: 0.5rem;
      
      @media screen and (max-width: 768px) {
        flex-direction: column;
        align-items: flex-start;
        
        .file-actions {
          margin-top: 0.5rem;
          width: 100%;
          
          button {
            width: 100%;
            margin-bottom: 0.25rem;
          }
        }
      }
    }
  }
}

/* 使用rem作为基础单位 */
:root {
  /* 基础字体大小，方便rem计算 */
  font-size: 16px;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  :root {
    font-size: 14px;
  }
  
  .el-table {
    .cell {
      padding: 0 5px;
    }
    
    th {
      padding: 8px 0;
    }
    
    td {
      padding: 8px 0;
    }
  }
  
  .el-button + .el-button {
    margin-left: 4px;
  }
  
  .el-form-item {
    margin-bottom: 12px;
  }
  
  .el-form-item__label {
    padding: 0 8px 0 0;
  }
}

/* 常用响应式类 */
.hidden-mobile {
  @media screen and (max-width: 768px) {
    display: none !important;
  }
}

.show-mobile {
  @media screen and (min-width: 769px) {
    display: none !important;
  }
}

/* 优化滚动性能 */
.scroll-container {
  -webkit-overflow-scrolling: touch;
  overflow-y: auto;
}

/* 硬件加速 */
.hardware-accelerated {
  transform: translateZ(0);
  backface-visibility: hidden;
  perspective: 1000px;
}

/* 加载状态优化 */
.loading-skeleton {
  @media screen and (max-width: 768px) {
    .skeleton-item {
      height: 3rem;
      margin-bottom: 0.5rem;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: loading 1.5s infinite;
    }
  }
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style> 