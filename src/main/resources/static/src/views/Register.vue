<template>
  <div class="register-page">
    <div class="register-wrapper">
      <div class="login-left">
        <div class="brand-info">
          <div class="logo-icon">✨</div>
          <h1 class="brand-name">Campus Find</h1>
          <p class="brand-slogan">加入校园互助生态，让失物重回主人怀抱。</p>
        </div>
        <div class="features-list">
          <div class="feature-item">🔒 严格的学号/工号身份准入机制</div>
          <div class="feature-item">⚡ 申请、审核、归还全流程即时感知</div>
          <div class="feature-item">🌿 纯净无广告，绿色健康的校园大厅</div>
        </div>
        <div class="login-footer">© 2026 校园失物招领运营团队</div>
      </div>

      <div class="login-right">
        <div class="form-container">
          <div class="form-header">
            <h2>开启您的探索</h2>
            <p>请填写以下信息完成校园通行账号创建</p>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" size="large" label-position="top">
            <el-form-item prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="请输入新学号 / 账号" 
                prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="请设置登录密码" 
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input 
                v-model="form.confirmPassword" 
                type="password" 
                placeholder="请再次输入密码以确认" 
                prefix-icon="Checked"
                show-password
              />
            </el-form-item>

            <el-form-item label="管理员授权秘钥（普通学生请直接留空）" prop="adminKey" class="key-item">
              <el-input 
                v-model="form.adminKey" 
                type="password" 
                placeholder="学生留空；管理员请输入专属授权码" 
                prefix-icon="Key"
                show-password
                clearable
              />
            </el-form-item>

            <el-button 
              type="primary" 
              class="submit-btn" 
              :loading="loading" 
              @click="handleRegister"
            >
              注 册
            </el-button>

            <div class="go-login">
              已有通行账号？<router-link to="/">立即登录</router-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api' 
import { ElMessage } from 'element-plus'
import { User, Lock, Checked, Key } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  adminKey: '' 
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致，请仔细检查！'))
  } else {
    callback()
  }
}

const rules = reactive({
  username: [
    { required: true, message: '注册账号不能为空', trigger: 'blur' },
    { min: 3, max: 15, message: '账号长度需在 3 到 15 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置您的登录密码', trigger: 'blur' },
    { min: 5, message: '为了账户安全，密码长度不能少于 5 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码确认', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

const handleRegister = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      const isConfiguringAdmin = form.adminKey && form.adminKey.trim() !== ''
      const finalRole = isConfiguringAdmin ? 1 : 0

      try {
        await register({
          username: form.username,
          password: form.password,
          role: finalRole,
          adminKey: form.adminKey
        })
        
        if (finalRole === 1) {
          ElMessage.success('超级管理员账号认证成功！已为您开启全局管控权限。')
        } else {
          ElMessage.success('学生通行账号注册成功！已自动建立校园电子服务档案。')
        }
        
        router.push('/')
      } catch (error) {
        console.error('注册异常:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-page {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: radial-gradient(circle at 10% 20%, rgb(239, 246, 255) 0%, rgb(219, 234, 254) 100%);
  overflow: hidden;
}

.register-wrapper {
  width: 1000px;
  height: 600px;
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 20px 40px -15px rgba(0, 0, 0, 0.08);
  display: flex;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.login-left {
  flex: 1.1;
  background: linear-gradient(135deg, #4f46e5 0%, #312e81 100%);
  padding: 50px;
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
}

.login-left::before {
  content: '';
  position: absolute;
  top: -20%;
  right: -20%;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.04);
}

.logo-icon { font-size: 36px; margin-bottom: 15px; }
.brand-name { font-size: 32px; font-weight: 800; margin: 0 0 10px 0; letter-spacing: 1px; }
.brand-slogan { font-size: 15px; color: #c7d2fe; margin: 0; }
.features-list { margin-top: 25px; }

.feature-item {
  margin-bottom: 16px;
  font-size: 14px;
  color: #e0e7ff;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.06);
  padding: 10px 15px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.login-footer { font-size: 12px; color: #818cf8; }
.login-right { flex: 1; display: flex; align-items: center; justify-content: center; background-color: #fff; padding: 40px 50px; }
.form-container { width: 100%; max-width: 340px; }
.form-header { margin-bottom: 25px; }
.form-header h2 { font-size: 24px; font-weight: 700; color: #0f172a; margin: 0 0 6px 0; }
.form-header p { font-size: 13px; color: #64748b; margin: 0; }

.key-item :deep(.el-form-item__label) {
  font-size: 12px !important;
  color: #64748b !important;
  margin-bottom: 4px !important;
  padding: 0 !important;
}

.submit-btn {
  width: 100%;
  padding: 12px 0 !important;
  font-size: 15px !important;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #4f46e5 0%, #4338ca 100%) !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
  margin-top: 5px;
}
.submit-btn:hover { box-shadow: 0 6px 20px rgba(79, 70, 229, 0.35); }
.go-login { text-align: center; margin-top: 20px; font-size: 13px; color: #64748b; }
.go-login a { color: #4f46e5; text-decoration: none; font-weight: 600; }
.go-login a:hover { text-decoration: underline; }

@media (max-width: 850px) {
  .register-wrapper { width: 420px; height: auto; }
  .login-left { display: none; }
}
</style>