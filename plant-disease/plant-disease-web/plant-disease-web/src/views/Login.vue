<template>
  <div style="display: flex; justify-content: center; align-items: center; height: 80vh">
    <el-card style="width: 400px">
      <h2 style="text-align: center; margin-bottom: 20px">植物病害识别系统</h2>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="80px">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" style="width: 100%" @click="handleLogin">登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="80px">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" show-password placeholder="请输入密码（至少6位）" />
            </el-form-item>
            <el-form-item>
              <el-button type="success" style="width: 100%" @click="handleRegister">注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const activeTab = ref('login')

// 登录表单
const loginFormRef = ref(null)
const loginForm = ref({ username: '', password: '' })
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 注册表单
const registerFormRef = ref(null)
const registerForm = ref({ username: '', password: '' })
const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    const res = await request.post('/user/login', loginForm.value)
    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userId', res.data.userId)
      localStorage.setItem('username', res.data.username)
      ElMessage.success('登录成功')
      router.push('/home')
    } else {
      ElMessage.error(res.msg || '登录失败')
    }
  } catch (err) {
    console.error('登录异常：', err)
  }
}

const handleRegister = async () => {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    const res = await request.post('/user/register', registerForm.value)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      registerForm.value = { username: '', password: '' }
      activeTab.value = 'login'
    } else {
      ElMessage.error(res.msg || '注册失败')
    }
  } catch (err) {
    console.error('注册异常：', err)
  }
}
</script>
