<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>作业提交系统</h2>
        <div v-if="serverStatus === 'offline'" class="server-status offline">
          <el-icon><WarningFilled /></el-icon> 服务器连接失败，部分功能可能不可用
        </div>
        <div v-else-if="serverStatus === 'online'" class="server-status online">
          <el-icon><SuccessFilled /></el-icon> 服务器连接正常
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
            <el-form-item prop="username">
              <el-select
                v-model="loginForm.username"
                placeholder="请选择用户名"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="user in userList"
                  :key="user"
                  :label="user"
                  :value="user"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleLogin" style="width: 100%">
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef">
            <el-form-item prop="username">
              <el-select
                v-model="registerForm.username"
                placeholder="请选择用户名"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="user in userList.filter(u => u !== 'admin')"
                  :key="user"
                  :label="user"
                  :value="user"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请确认密码"
                show-password
                @keyup.enter="handleRegister"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleRegister" style="width: 100%">
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="login-tips">
        <p>首次登录请先选择用户名并设置密码</p>
        <p>管理员账号：admin</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import axios from 'axios'
import { WarningFilled, SuccessFilled } from '@element-plus/icons-vue'

const router = useRouter()
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const activeTab = ref('login')

// 用户列表（按拼音字典序排序）
const userList = [
  'admin', // 管理员账号放在最前面
  '段鹏鹏', '方震', '高天全', '高子涵', '龚皓',
  '霍雨婷', '贾奇燠', '贾一丁', '金闿翎', '靳宇晨',
  '李希', '刘森源', '刘文晶', '吕彦儒', '吕泽熙',
  '苏宗佑', '汤锦健', '王奕涵', '王悦琳', '王志成',
  '王墨', '王泽君', '夏正鑫', '肖俊波', '杨海屹',
  '杨孟涵', '张成宇', '张峻菘', '张钟文', '赵萌'
]

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const loginRules = {
  username: [
    { required: true, message: '请选择用户名', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
  ]
}

const registerRules = {
  username: [
    { required: true, message: '请选择用户名', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 服务器状态
const serverStatus = ref<'checking' | 'online' | 'offline'>('checking')

// 检查服务器连接状态
const checkServerStatus = async () => {
  try {
    // 尝试请求一个简单的API端点
    await axios.get('/health', { timeout: 5000 })
    serverStatus.value = 'online'
    // 更新全局服务器状态
    import('../router').then(({ setServerOffline }) => {
      setServerOffline(false)
    })
  } catch (error) {
    console.error('服务器连接检查失败:', error)
    serverStatus.value = 'offline'
    // 更新全局服务器状态
    import('../router').then(({ setServerOffline }) => {
      setServerOffline(true)
    })
  }
}

onMounted(() => {
  // 页面加载时检查服务器状态
  checkServerStatus()
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (serverStatus.value === 'offline') {
          ElMessage.warning('服务器连接失败，无法登录。请稍后再试。')
          return
        }

        const response = await axios.post('/auth/login', {
          username: loginForm.username,
          password: loginForm.password
        })

        if (response.data.success) {
          localStorage.setItem('token', response.data.token)
          localStorage.setItem('isAdmin', String(response.data.isAdmin))
          localStorage.setItem('username', response.data.username)
          
          ElMessage.success(response.data.message)
          router.push(response.data.isAdmin ? '/admin' : '/dashboard')
        }
      } catch (error: any) {
        console.error('登录错误:', error)
        if (!error.response) {
          ElMessage.error('无法连接到服务器，请检查网络连接')
          serverStatus.value = 'offline'
        } else {
          ElMessage.error(error.response?.data?.error || '登录失败')
        }
      }
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 如果服务器离线，显示提示
        if (serverStatus.value === 'offline') {
          ElMessage.warning('服务器连接失败，无法注册。请稍后再试。')
          return
        }

        const response = await axios.post('/auth/register', {
          username: registerForm.username,
          password: registerForm.password
        })

        if (response.data.success) {
          ElMessage.success(response.data.message)
          // 注册成功后切换到登录页
          activeTab.value = 'login'
          loginForm.username = registerForm.username
          // 清空注册表单
          registerFormRef.value?.resetFields()
        }
      } catch (error: any) {
        if (!error.response) {
          // 网络错误或服务器未响应
          ElMessage.error('无法连接到服务器，请检查网络连接')
          serverStatus.value = 'offline'
        } else {
          ElMessage.error(error.response?.data?.error || '注册失败')
        }
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  padding: 1rem;
  
  @media screen and (max-width: 768px) {
    padding: 0.5rem;
    align-items: flex-start;
    padding-top: 2rem;
  }
}

.login-card {
  width: 400px;
  transition: all 0.3s ease;
  
  @media screen and (max-width: 768px) {
    width: 100%;
    margin: 0;
    border-radius: 0;
  }
}

.login-card :deep(.el-card__header) {
  text-align: center;
  
  @media screen and (max-width: 768px) {
    padding: 1rem;
    
    h2 {
      font-size: 1.3rem;
      margin: 0;
    }
  }
}

.login-card :deep(.el-card__body) {
  @media screen and (max-width: 768px) {
    padding: 1rem;
  }
}

.login-card :deep(.el-tabs__header) {
  @media screen and (max-width: 768px) {
    margin-bottom: 1rem;
  }
}

.login-card :deep(.el-form-item) {
  @media screen and (max-width: 768px) {
    margin-bottom: 1rem;
  }
}

.login-card :deep(.el-input__wrapper) {
  @media screen and (max-width: 768px) {
    padding: 0.5rem;
  }
}

.login-card :deep(.el-button) {
  @media screen and (max-width: 768px) {
    padding: 0.6rem 1rem;
    font-size: 0.95rem;
  }
}

.login-card :deep(.el-select) {
  @media screen and (max-width: 768px) {
    width: 100%;
  }
}

.login-tips {
  margin-top: 20px;
  color: #909399;
  font-size: 14px;
  text-align: center;
  
  @media screen and (max-width: 768px) {
    margin-top: 1rem;
    font-size: 0.8rem;
  }
}

.login-tips p {
  margin: 5px 0;
  
  @media screen and (max-width: 768px) {
    margin: 3px 0;
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

/* 优化下拉选择器在移动端的显示 */
:deep(.el-select-dropdown) {
  @media screen and (max-width: 768px) {
    width: 90vw !important;
    margin: 0 auto;
    
    .el-select-dropdown__item {
      padding: 0.8rem 1rem;
      font-size: 0.95rem;
    }
  }
}

/* 优化表单验证消息在移动端的显示 */
:deep(.el-form-item__error) {
  @media screen and (max-width: 768px) {
    font-size: 0.8rem;
    padding-top: 0.2rem;
  }
}

.server-status {
  font-size: 14px;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.offline {
  color: #f56c6c;
}

.online {
  color: #67c23a;
}
</style> 