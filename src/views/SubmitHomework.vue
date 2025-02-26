<template>
  <div class="submit-container">
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
        <el-card>
          <template #header>
            <div class="card-header">
              <span>提交作业</span>
              <el-button @click="$router.push('/dashboard')">返回</el-button>
            </div>
          </template>
          
          <el-form 
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="100px"
          >
            <el-form-item label="作业标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入作业标题" />
            </el-form-item>
            
            <el-form-item label="作业描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                rows="4"
                placeholder="请输入作业描述"
              />
            </el-form-item>
            
            <el-form-item label="作业文件" prop="file">
              <el-upload
                class="upload-demo"
                drag
                action="#"
                :auto-upload="false"
                :on-change="handleFileChange"
                accept=".pdf"
              >
                <el-icon class="el-icon--upload">
                  <upload-filled />
                </el-icon>
                <div class="el-upload__text">
                  将文件拖到此处，或<em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    只能上传 PDF 文件
                  </div>
                </template>
              </el-upload>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="submitForm">提交</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import type { FormInstance, UploadFile } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const username = ref(localStorage.getItem('username') || '')

const form = reactive({
  title: '',
  description: '',
  file: null as File | null
})

const rules = {
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度应在2到50个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入作业描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度应在10到500个字符之间', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请上传作业文件', trigger: 'change' }
  ]
}

const handleFileChange = (uploadFile: UploadFile) => {
  if (uploadFile.raw) {
    if (uploadFile.raw.type !== 'application/pdf') {
      ElMessage.error('只能上传PDF文件！')
      return false
    }
    form.file = uploadFile.raw
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      // TODO: 实现文件上传和作业提交逻辑
      ElMessage.success('作业提交成功！')
      router.push('/dashboard')
    }
  })
}

const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  form.file = null
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
</script>

<style scoped>
.submit-container {
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

.upload-demo {
  width: 100%;
}
</style> 