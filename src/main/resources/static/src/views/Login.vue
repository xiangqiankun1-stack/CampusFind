<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card animate-fade-in">
      <div class="card-header">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <el-icon><Search /></el-icon>
          </div>
        </div>
        <h2 class="title">校园失物招领</h2>
        <p class="subtitle">Campus Lost & Found System</p>
      </div>

      <el-form
          :model="form"
          label-position="top"
          class="login-form"
          @keyup.enter="handleLogin"
      >
        <el-form-item>
          <div class="input-wrapper">
            <el-icon class="input-icon"><User /></el-icon>
            <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                size="large"
                class="custom-input"
                clearable
            />
          </div>
        </el-form-item>

        <el-form-item>
          <div class="input-wrapper">
            <el-icon class="input-icon"><Lock /></el-icon>
            <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                show-password
                size="large"
                class="custom-input"
            />
          </div>
        </el-form-item>

        <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>

        <div class="footer-links">
          <el-button
              type="text"
              class="link-btn"
              @click="goRegister"
          >
            <el-icon><CirclePlus /></el-icon>
            还没有账号？立即注册
          </el-button>
        </div>
      </el-form>

      <div class="card-footer">
        <p class="tips-text">💡 提示：请使用学校统一身份认证账号登录</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api'
import { ElMessage } from 'element-plus'
import { Search, User, Lock, CirclePlus } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码！')
    return
  }

  loading.value = true
  try {
    const res = await login(form)
    if (res) {
      ElMessage.success('🎉 登录成功！欢迎回来')
      localStorage.setItem('user', JSON.stringify(res))

      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push('/home')
      }, 500)
    }
  } catch (error) {
    console.error('登录异常', error)
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

const goRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
/* 🎨 容器与背景 */
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

/* 背景装饰圆圈 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 20s infinite ease-in-out;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -50px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  left: -50px;
  animation-delay: 5s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 20%;
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.1;
  }
  50% {
    transform: translateY(-30px) rotate(180deg);
    opacity: 0.2;
  }
}

/* 📦 登录卡片 */
.login-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 48px 40px;
  position: relative;
  z-index: 1;
}

.animate-fade-in {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 🎯 卡片头部 */
.card-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-wrapper {
  margin-bottom: 20px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.logo-icon .el-icon {
  font-size: 40px;
  color: white;
}

.title {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: 1px;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #64748b;
  font-weight: 400;
  letter-spacing: 2px;
}

/* 📝 表单样式 */
.login-form {
  margin-top: 32px;
}
.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}
.login-form :deep(.el-form-item__content) {
  width: 100%;
}
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 16px;
  font-size: 18px;
  color: #94a3b8;
  z-index: 1;
  transition: color 0.3s;
}

.custom-input {
  width: 100%;
}

.custom-input :deep(.el-input__wrapper) {
  width: 100% !important;
  padding-left: 48px !important;
  padding-right: 16px !important;
  border-radius: 12px !important;
  background-color: #f8fafc !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  border: 2px solid transparent !important;
  transition: all 0.3s ease !important;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #cbd5e1 !important;
  background-color: #fff !important;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea !important;
  background-color: #fff !important;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1) !important;
}

.custom-input:focus-within .input-icon {
  color: #667eea;
}

/* 🔘 登录按钮 */
.login-btn {
  width: 100%;
  margin-top: 24px;
  height: 50px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(102, 126, 234, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

/* 🔗 底部链接 */
.footer-links {
  margin-top: 20px;
  text-align: center;
}

.link-btn {
  font-size: 14px;
  color: #667eea;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
}

.link-btn:hover {
  color: #764ba2;
  transform: translateX(4px);
}

.link-btn .el-icon {
  font-size: 16px;
}

/* 💡 提示信息 */
.card-footer {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f1f5f9;
}

.tips-text {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
  line-height: 1.5;
}

/* 📱 响应式优化 */
@media (max-width: 480px) {
  .login-card {
    padding: 36px 24px;
    border-radius: 20px;
  }

  .title {
    font-size: 24px;
  }

  .logo-icon {
    width: 70px;
    height: 70px;
  }

  .logo-icon .el-icon {
    font-size: 36px;
  }
}
</style>
