<template>
  <div class="admin-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>作业提交系统 - 管理员</h2>
          <div class="user-info">
            <span>{{ username }}</span>
            <el-button type="text" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <el-row :gutter="20">
          <!-- 作业发布管理 -->
          <el-col :span="24" style="margin-bottom: 20px">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>作业发布管理</span>
                  <el-button type="primary" @click="showPublishDialog">
                    发布新作业
                  </el-button>
                </div>
              </template>
              
              <el-table :data="assignmentList" style="width: 100%">
                <el-table-column prop="title" label="作业标题" />
                <el-table-column prop="description" label="作业说明" show-overflow-tooltip />
                <el-table-column prop="deadline" label="截止日期" width="180" />
                <el-table-column prop="createTime" label="发布时间" width="180" />
                <el-table-column label="文件格式" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.fileFormat === 'pdf' ? 'warning' : 'info'">
                      {{ row.fileFormat === 'pdf' ? '仅PDF' : '不限' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="提交控制" width="120">
                  <template #default="{ row }">
                    <el-switch
                      v-model="row.allowSubmit"
                      active-text="允许"
                      inactive-text="禁止"
                      @change="handleSubmitControl(row)"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="{ row }">
                    <el-button 
                      type="primary" 
                      link
                      @click="viewSubmissions(row)"
                    >
                      查看提交情况
                    </el-button>
                    <el-button 
                      type="danger" 
                      link
                      @click="deleteAssignment(row)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>

          <!-- 作业提交情况 -->
          <el-col :span="24">
            <el-card v-if="currentAssignment">
              <template #header>
                <div class="card-header">
                  <div class="header-left">
                    <span>{{ currentAssignment.title }} - 提交情况</span>
                    <el-tag 
                      :type="currentAssignment.allowSubmit ? 'success' : 'danger'"
                      style="margin-left: 10px"
                    >
                      {{ currentAssignment.allowSubmit ? '允许提交' : '禁止提交' }}
                    </el-tag>
                  </div>
                  <div>
                    <el-input
                      v-model="searchQuery"
                      placeholder="搜索用户"
                      style="width: 200px; margin-right: 10px"
                    >
                      <template #prefix>
                        <el-icon><search /></el-icon>
                      </template>
                    </el-input>
                    <el-select v-model="statusFilter" placeholder="状态筛选">
                      <el-option label="全部" value="" />
                      <el-option label="已提交" value="submitted" />
                      <el-option label="未提交" value="unsubmitted" />
                    </el-select>
                  </div>
                </div>
              </template>
              
              <el-table :data="filteredSubmissionList" style="width: 100%">
                <el-table-column prop="submittedBy" label="用户" width="120">
                  <template #default="{ row }">
                    {{ typeof row.submittedBy === 'string' ? row.submittedBy : row.submittedBy }}
                  </template>
                </el-table-column>
                <el-table-column prop="submitTime" label="提交时间" width="180" />
                <el-table-column prop="originalFilename" label="文件名" width="180">
                  <template #default="{ row }">
                    {{ row.originalFilename || (row.status === 'submitted' ? '未知文件名' : '-') }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="getStatusType(row.status)">
                      {{ getStatusText(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="{ row }">
                    <el-button 
                      v-if="row.status === 'submitted'"
                      type="primary" 
                      link
                      @click="viewSubmission(row)"
                    >
                      查看作业
                    </el-button>
                    <el-button 
                      type="warning" 
                      link
                      @click="addFeedback(row)"
                    >
                      添加反馈
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div class="submission-stats" style="margin-top: 20px">
                <el-descriptions :column="4" border>
                  <el-descriptions-item label="总人数">{{ userList.length }}</el-descriptions-item>
                  <el-descriptions-item label="已提交">{{ submittedCount }}</el-descriptions-item>
                  <el-descriptions-item label="未提交">{{ unsubmittedCount }}</el-descriptions-item>
                  <el-descriptions-item label="提交率">{{ submissionRate }}%</el-descriptions-item>
                </el-descriptions>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>

    <!-- 发布作业对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布新作业"
      width="500px"
    >
      <el-form :model="publishForm" :rules="publishRules" ref="publishFormRef">
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="publishForm.title" placeholder="请输入作业标题" />
        </el-form-item>
        
        <el-form-item label="作业说明" prop="description">
          <el-input
            v-model="publishForm.description"
            type="textarea"
            rows="4"
            placeholder="请输入作业说明"
          />
        </el-form-item>

        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            v-model="publishForm.deadline"
            type="datetime"
            placeholder="选择截止日期"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            :disabled-date="disabledDate"
          />
        </el-form-item>

        <el-form-item label="文件格式">
          <el-radio-group v-model="publishForm.fileFormat">
            <el-radio label="pdf">仅PDF</el-radio>
            <el-radio label="any">不限格式</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="提交控制">
          <el-switch
            v-model="publishForm.allowSubmit"
            active-text="允许提交"
            inactive-text="禁止提交"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="publishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePublish">
            发布
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 反馈对话框 -->
    <el-dialog
      v-model="feedbackDialogVisible"
      title="添加反馈"
      width="500px"
    >
      <el-form :model="feedbackForm">
        <el-form-item label="反馈内容" :label-width="'100px'">
          <el-input
            v-model="feedbackForm.content"
            type="textarea"
            rows="4"
            placeholder="请输入反馈内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="feedbackDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitFeedback">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import type { HomeworkAssignment, HomeworkSubmission } from '../types/homework'
import axios from 'axios'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '')
const searchQuery = ref('')
const statusFilter = ref('')
const publishDialogVisible = ref(false)
const feedbackDialogVisible = ref(false)
// const submissionDialogVisible = ref(false) // 已注释：未使用
const currentSubmission = ref<HomeworkSubmission | null>(null)
const currentAssignment = ref<HomeworkAssignment | null>(null)
const publishFormRef = ref<FormInstance>()

// 模拟用户列表
const userList = [
  'admin', // 管理员账号放在最前面
  '段鹏鹏', '方震', '高天全', '高子涵', '龚皓',
  '霍雨婷', '贾奇燠', '贾一丁', '金闿翎', '靳宇晨',
  '李希', '刘森源', '刘文晶', '吕彦儒', '吕泽熙',
  '苏宗佑', '汤锦健', '王奕涵', '王悦琳', '王志成',
  '王墨', '王泽君', '夏正鑫', '肖俊波', '杨海屹',
  '杨孟涵', '张成宇', '张峻菘', '张钟文', '赵萌'
]

// 作业列表
const assignmentList = ref<HomeworkAssignment[]>([])
// 提交记录列表
const submissionList = ref<HomeworkSubmission[]>([])

// 获取作业列表
const fetchAssignments = async () => {
  try {
    const response = await axios.get('/assignments')
    assignmentList.value = response.data
  } catch (error: any) {
    ElMessage.error(error.response?.data?.error || '获取作业列表失败')
  }
}

// 在组件挂载时获取作业列表
onMounted(() => {
  fetchAssignments()
})

const publishForm = reactive({
  title: '',
  description: '',
  deadline: '',
  allowSubmit: true,
  fileFormat: 'pdf'
})

const publishRules = {
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度应在2到50个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入作业说明', trigger: 'blur' }
  ],
  deadline: [
    { required: true, message: '请选择截止日期', trigger: 'change' }
  ]
}

const feedbackForm = ref({
  content: ''
})

const filteredSubmissionList = computed(() => {
  if (!currentAssignment.value) return []

  console.log('当前作业ID:', currentAssignment.value.id);
  console.log('提交记录列表:', submissionList.value);

  // 创建完整的提交记录列表（包括未提交的用户）
  const allSubmissions = userList
    .filter(user => user !== 'admin') // 排除管理员
    .map(user => {
      // 查找该用户对当前作业的提交记录
      const submission = submissionList.value.find(
        (s: HomeworkSubmission) => {
          // 检查assignmentId是否为数字，如果是字符串则转换
          const assignmentId = typeof s.assignmentId === 'string' 
            ? parseInt(s.assignmentId) 
            : s.assignmentId;
          
          const currentAssignmentId = typeof currentAssignment.value!.id === 'string'
            ? parseInt(currentAssignment.value!.id)
            : currentAssignment.value!.id;
            
          // 获取提交者用户名
          let submittedBy = typeof s.submittedBy === 'string' ? s.submittedBy : '';
          
          const match = assignmentId === currentAssignmentId && submittedBy === user;
                       
          console.log(`检查用户 ${user} 的提交记录:`, 
                     {assignmentId: s.assignmentId, 
                      currentAssignmentId: currentAssignment.value!.id,
                      submittedBy: submittedBy, 
                      match});
          return match;
        }
      );
      
      if (submission) {
        console.log(`用户 ${user} 已提交作业`);
        // 确保返回的对象中submittedBy是字符串
        return {
          ...submission,
          submittedBy: user
        };
      } else {
        console.log(`用户 ${user} 未提交作业`);
        return {
          id: 0,
          assignmentId: currentAssignment.value!.id,
          title: currentAssignment.value!.title,
          submitTime: '-',
          status: 'unsubmitted' as const,
          file: null,
          submittedBy: user
        };
      }
    });

  return allSubmissions.filter(submission => {
    const submittedBy = typeof submission.submittedBy === 'string' 
      ? submission.submittedBy 
      : submission.submittedBy || '';
      
    const matchesSearch = searchQuery.value === '' || 
      submittedBy.toLowerCase().includes(searchQuery.value.toLowerCase());
    
    const matchesStatus = statusFilter.value === '' || 
      submission.status === statusFilter.value;
    
    return matchesSearch && matchesStatus;
  });
})

// 统计信息
const submittedCount = computed(() => 
  filteredSubmissionList.value.filter(s => s.status === 'submitted').length
)

const unsubmittedCount = computed(() => 
  filteredSubmissionList.value.filter(s => s.status === 'unsubmitted').length
)

const submissionRate = computed(() => {
  const total = filteredSubmissionList.value.length
  return total > 0 ? Math.round((submittedCount.value / total) * 100) : 0
})

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    submitted: 'success',
    unsubmitted: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    submitted: '已提交',
    unsubmitted: '未提交'
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

const showPublishDialog = () => {
  publishForm.title = ''
  publishForm.description = ''
  publishForm.deadline = ''
  publishForm.allowSubmit = true
  publishForm.fileFormat = 'pdf'
  publishDialogVisible.value = true
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now()
}

const handlePublish = async () => {
  if (!publishFormRef.value) return
  
  await publishFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 格式化日期时间
        const formattedDeadline = publishForm.deadline ? 
          publishForm.deadline.replace(' ', 'T') + ':00' : null

        const response = await axios.post('/assignments', {
          title: publishForm.title,
          description: publishForm.description,
          deadline: formattedDeadline,
          allowSubmit: publishForm.allowSubmit,
          fileFormat: publishForm.fileFormat
        })
        
        assignmentList.value.unshift(response.data)
        ElMessage.success('作业发布成功！')
        publishDialogVisible.value = false
      } catch (error: any) {
        console.error('发布作业失败:', error)
        ElMessage.error(error.response?.data?.error || '发布作业失败，请检查网络连接')
      }
    }
  })
}

const handleSubmitControl = async (assignment: HomeworkAssignment) => {
  try {
    await axios.put(`/assignments/${assignment.id}/submit-control`, {
      allowSubmit: assignment.allowSubmit
    })
    const action = assignment.allowSubmit ? '允许' : '禁止'
    ElMessage.success(`已${action}作业提交`)
  } catch (error: any) {
    ElMessage.error(error.response?.data?.error || '更新提交控制失败')
    // 恢复原始状态
    assignment.allowSubmit = !assignment.allowSubmit
  }
}

const deleteAssignment = (assignment: HomeworkAssignment) => {
  ElMessageBox.confirm('确定要删除这个作业吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.delete(`/assignments/${assignment.id}`)
      assignmentList.value = assignmentList.value.filter(item => item.id !== assignment.id)
      if (currentAssignment.value?.id === assignment.id) {
        currentAssignment.value = null
      }
      ElMessage.success('删除成功')
    } catch (error: any) {
      ElMessage.error(error.response?.data?.error || '删除作业失败')
    }
  })
}

const viewSubmissions = async (assignment: HomeworkAssignment) => {
  try {
    // 验证作业ID
    if (!assignment?.id) {
      ElMessage.error('无效的作业ID')
      return
    }

    // 验证token
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录已过期，请重新登录')
      router.push('/')
      return
    }

    console.log('正在获取作业提交情况:', {
      assignmentId: assignment.id,
      title: assignment.title
    })

    const response = await axios.get(`/submissions/assignment/${assignment.id}`)
    console.log('获取提交情况成功:', response.data)
    
    submissionList.value = response.data
    currentAssignment.value = assignment
  } catch (error: any) {
    console.error('获取提交情况失败:', error)
    
    if (error.response) {
      const status = error.response.status
      const errorMessage = error.response.data?.error || '未知错误'
      
      if (status === 400) {
        ElMessage.error(`请求参数错误: ${errorMessage}`)
      } else if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        router.push('/')
      } else if (status === 403) {
        ElMessage.error('没有权限查看此作业的提交情况')
      } else if (status === 404) {
        ElMessage.error('作业不存在')
      } else {
        ElMessage.error(`获取提交情况失败: ${errorMessage}`)
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
  }
}

const viewSubmission = async (submission: HomeworkSubmission) => {
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
      // 尝试从submittedBy获取用户名
      const username = typeof submission.submittedBy === 'string' ? submission.submittedBy : '';
      
      originalFilename = `${username}-作业-${submission.id}.pdf`;
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
    
    // 使用axios获取文件，设置responseType为blob
    const response = await axios.get(`/submissions/${submission.id}/file`, {
      responseType: 'blob',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    
    if (!response.data) {
      throw new Error(`下载失败: 未收到文件数据`);
    }
    
    // 获取文件内容
    const blob = response.data;
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

const addFeedback = (submission: HomeworkSubmission) => {
  currentSubmission.value = submission
  feedbackForm.value.content = submission.feedback || ''
  feedbackDialogVisible.value = true
}

const submitFeedback = async () => {
  if (!feedbackForm.value.content.trim()) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  
  if (currentSubmission.value) {
    try {
      const response = await axios.put(
        `/submissions/${currentSubmission.value.id}/feedback`,
        { feedback: feedbackForm.value.content }
      )
      
      const index = submissionList.value.findIndex(item => item.id === currentSubmission.value?.id)
      if (index !== -1) {
        submissionList.value[index] = response.data
      }
      
      ElMessage.success('反馈提交成功')
      feedbackDialogVisible.value = false
    } catch (error: any) {
      ElMessage.error(error.response?.data?.error || '提交反馈失败')
    }
  }
}
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background-color: #f5f7fa;
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
      font-size: 1.1rem;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  @media screen and (max-width: 768px) {
    flex-direction: column;
    gap: 10px;
    
    .header-left {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    
    .el-input {
      width: 100% !important;
      margin-bottom: 10px;
    }
    
    .el-select {
      width: 100%;
    }
  }
}

.el-table {
  @media screen and (max-width: 768px) {
    font-size: 0.9rem;
    
    :deep(.el-table__header) {
      font-size: 0.9rem;
    }
    
    :deep(.el-button--link) {
      padding: 4px;
    }
    
    :deep(.el-switch__label) {
      font-size: 0.8rem;
    }
  }
}

.submission-stats {
  @media screen and (max-width: 768px) {
    :deep(.el-descriptions__cell) {
      padding: 8px !important;
    }
    
    :deep(.el-descriptions__label) {
      font-size: 0.9rem;
    }
    
    :deep(.el-descriptions__content) {
      font-size: 0.9rem;
    }
  }
}

.el-dialog {
  @media screen and (max-width: 768px) {
    width: 90% !important;
    margin: 0 auto;
    
    :deep(.el-dialog__body) {
      padding: 15px;
    }
    
    :deep(.el-form-item__label) {
      font-size: 0.9rem;
    }
    
    :deep(.el-input__wrapper) {
      padding: 0 10px;
    }
    
    :deep(.el-textarea__inner) {
      min-height: 80px !important;
    }
  }
}

/* 移动端表格优化 */
@media screen and (max-width: 768px) {
  .el-table {
    :deep(.el-table__body-wrapper) {
      overflow-x: auto;
    }
    
    :deep(.el-table__row) {
      td {
        padding: 8px 0;
      }
    }
    
    :deep(.el-button--link) {
      margin-left: 0;
      padding: 4px 8px;
    }
  }
}

/* 移动端日期选择器优化 */
:deep(.el-date-editor) {
  @media screen and (max-width: 768px) {
    width: 100% !important;
  }
}

/* 移动端开关组件优化 */
:deep(.el-switch) {
  @media screen and (max-width: 768px) {
    .el-switch__label {
      font-size: 0.8rem;
    }
  }
}

/* 移动端标签优化 */
:deep(.el-tag) {
  @media screen and (max-width: 768px) {
    font-size: 0.8rem;
    padding: 0 4px;
  }
}

/* 移动端按钮优化 */
:deep(.el-button) {
  @media screen and (max-width: 768px) {
    padding: 8px 15px;
    font-size: 0.9rem;
    
    &.el-button--text {
      padding: 0;
    }
  }
}

/* 移动端表单项间距优化 */
:deep(.el-form-item) {
  @media screen and (max-width: 768px) {
    margin-bottom: 15px;
  }
}

/* 触摸反馈 */
@media screen and (max-width: 768px) {
  .el-button {
    &:active {
      transform: scale(0.98);
    }
  }
  
  .el-input__wrapper {
    &:active {
      background-color: #f5f7fa;
    }
  }
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

/* 移动端单选框组优化 */
:deep(.el-radio-group) {
  @media screen and (max-width: 768px) {
    display: flex;
    flex-direction: column;
    gap: 10px;
    
    .el-radio {
      margin-right: 0;
      padding: 5px 0;
    }
  }
}
</style> 