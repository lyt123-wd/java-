<template>
  <div style="min-height: 100vh; background: #f5faf6">
    <!-- 顶部导航栏：深绿色主题 -->
    <el-header class="app-header">
      <div class="app-header-left">
        <span class="app-title">🌿 植物叶片病害识别与防治系统</span>
        <el-menu
          mode="horizontal"
          class="app-menu"
          :default-active="activeMenu"
          background-color="#1b5e20"
          text-color="#c8e6c9"
          active-text-color="#ffffff"
          :ellipsis="true"
          router
        >
          <el-menu-item index="/home">数据概览</el-menu-item>
          <el-menu-item index="/recognize">病害识别</el-menu-item>
          <el-menu-item index="/record">历史记录</el-menu-item>
          <el-menu-item index="/statistics">数据统计</el-menu-item>
          <el-menu-item index="/disease">病害知识库</el-menu-item>
        </el-menu>
      </div>

      <!-- 用户下拉菜单 -->
      <el-dropdown class="app-user" @command="handleUserCommand">
        <span style="color: #fff; cursor: pointer; display: flex; align-items: center">
          <el-icon style="margin-right: 6px"><User /></el-icon>
          {{ username }}
          <el-icon style="margin-left: 6px; font-size: 12px"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="password">修改密码</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </el-header>

    <!-- 主内容区 -->
    <el-main>
      <router-view />
    </el-main>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpdatePwd">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, ArrowDown } from '@element-plus/icons-vue'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)
const username = ref(localStorage.getItem('username') || '用户')
const pwdVisible = ref(false)
const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 用户菜单操作
const handleUserCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定退出当前账号吗？', '提示', { type: 'warning' })
      .then(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('userId')
        ElMessage.success('已退出登录')
        router.push('/login')
      })
      .catch(() => {})
  } else if (command === 'password') {
    pwdVisible.value = true
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  }
}

// 提交修改密码
const submitUpdatePwd = async () => {
  const { oldPassword, newPassword, confirmPassword } = pwdForm.value
  if (!oldPassword || !newPassword || !confirmPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (newPassword !== confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  if (newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  try {
    const res = await request.post('/user/updatePassword', {
      oldPassword,
      newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      pwdVisible.value = false
      localStorage.removeItem('token')
      router.push('/login')
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch {
    ElMessage.error('修改失败，请重试')
  }
}
</script>

<style scoped>
/* 顶部导航栏布局（窄屏适配见底部媒体查询） */
.el-header.app-header {
  background: #1b5e20;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.app-header-left {
  display: flex;
  align-items: center;
  flex: 1;
  /* 允许菜单收缩，触发 el-menu 自带的省略号折叠 */
  min-width: 0;
}
.app-title {
  font-size: 18px;
  font-weight: bold;
  margin-right: 40px;
  white-space: nowrap;
}
.app-menu {
  min-width: 0;
}
.app-user {
  flex-shrink: 0;
  margin-left: 12px;
}

/* 中等宽度：压缩标题与间距 */
@media (max-width: 1100px) {
  .app-title {
    margin-right: 16px;
    font-size: 16px;
  }
  .el-header.app-header {
    padding: 0 12px;
  }
}

/* 较窄屏幕：隐藏系统标题，只保留菜单与用户区 */
@media (max-width: 900px) {
  .app-title {
    display: none;
  }
}

/* 手机宽度：允许换行，菜单整行展示 */
@media (max-width: 640px) {
  .el-header.app-header {
    flex-wrap: wrap;
    height: auto;
    padding: 6px 10px;
  }
  .app-header-left {
    flex-basis: 100%;
  }
  .app-user {
    margin-left: auto;
    line-height: 40px;
  }
}
</style>
