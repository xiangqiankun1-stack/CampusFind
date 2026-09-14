<template>
  <el-container class="layout-container animate-fade-in notranslate">
    <el-aside width="240px" class="custom-aside">
      
      <div class="sidebar-brand">
        <div class="brand-logo">🔍</div>
        <div class="brand-text">
          <h2>Campus Find</h2>
          <p>校园失物招领系统</p>
        </div>
      </div>

      <el-menu
        :default-active="route.path"
        router
        class="custom-menu"
        background-color="transparent"
        text-color="#64748b"
        active-text-color="#4f46e5"
      >
        <el-menu-item index="/home" class="menu-item-btn">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据大厅</span>
        </el-menu-item>

        <template v-if="user.role === 0">
          <el-menu-item index="/add" class="menu-item-btn">
            <el-icon><CirclePlus /></el-icon>
            <span>发布寻物/招领</span>
          </el-menu-item>

          <el-menu-item index="/my-claims" class="menu-item-btn">
            <el-icon><Files /></el-icon>
            <span>我的认领记录</span>
          </el-menu-item>

          <el-menu-item index="/my-pending-claims" class="menu-item-btn">
            <el-icon><Bell /></el-icon>
            <span>待确认认领</span>
          </el-menu-item>

          <el-menu-item index="/my-publish" class="menu-item-btn">
            <el-icon><Document /></el-icon>
            <span>我的发布</span>
          </el-menu-item>
        </template>
        


        <template v-if="user.role === 1">
          <el-menu-item index="/pending" class="menu-item-btn">
            <el-icon><Checked /></el-icon>
            <span>审核发布申请</span>
          </el-menu-item>

        <!--   <el-menu-item index="/claims" class="menu-item-btn">
            <el-icon><Stamp /></el-icon>
            <span>处理认领核对</span>
          </el-menu-item> -->
        </template>
      </el-menu>

      <div class="sidebar-user-panel" v-if="user.username">
        <div class="user-avatar-mini">
          {{ user.username?.substring(0, 2).toUpperCase() }}
        </div>
        <div class="user-meta-info">
          <span class="username-text">{{ user.username }}</span>
          <el-tag :type="user.role === 1 ? 'danger' : 'primary'" size="small" effect="dark" class="role-badge">
            {{ user.role === 1 ? '系统管理员' : '认证学生' }}
          </el-tag>
        </div>
      </div>
    </el-aside>

    <el-container class="right-main-container">
      <el-header height="64px" class="custom-header">
        <div class="header-breadcrumb">
          <span class="crumb-parent">工作台</span>
          <span class="crumb-separator">/</span>
          <span class="crumb-current">{{ currentMenuName }}</span>
        </div>
        
        <div class="header-actions">
          <el-button type="danger" plain size="default" class="logout-btn" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon><span>退出系统</span>
          </el-button>
        </div>
      </el-header>

      <el-main class="custom-main">
        <div class="main-content-card">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { DataAnalysis, CirclePlus, Files, Checked, Stamp, SwitchButton,Document, Bell } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 🌟 核心加固：去除破坏响应式的末尾 .value，实现动态用户数据监听
const user = computed(() => {
  try {
    const cached = localStorage.getItem('user')
    if (!cached) return { username: 'GUEST', role: 0 }
    const parsed = JSON.parse(cached)
    parsed.role = Number(parsed.role) // 强制类型安全转换
    return parsed
  } catch (e) {
    return { username: 'GUEST', role: 0 }
  }
})

// 面包屑联动
const currentMenuName = computed(() => {
  switch (route.path) {
    case '/home': return '失物招领数据大厅'
    case '/add': return '发布物品档案'
    case '/my-claims': return '个人认领底账'
    case '/pending': return '待审核物品清单'
    case '/claims': return '全量认领流转审核'
    default: return '核心工作台'
  }
})

// 🌟 三重保险安全退出函数
const handleLogout = () => {
  ElMessageBox.confirm('确定要安全退出校园失物招领管理系统吗？', '提示', {
    confirmButtonText: '确定登出',
    cancelButtonText: '留在此页',
    type: 'warning'
  }).then(() => {
    // 1. 斩断浏览器缓存数据
    localStorage.removeItem('user')
    localStorage.clear() 
    
    ElMessage({
      type: 'success',
      message: '正在退出系统...',
      duration: 1000
    })

    // 2. 路由跳回真正的根登录路径
    router.push('/').then(() => {
      // 3. 终极底牌：浏览器原生重载，彻底扫空内存中由于多角色切换残留的路由缓存
      window.location.reload()
    })
  }).catch(() => {})
}
</script>

<style scoped>
.layout-container { height: 100vh; width: 100vw; background-color: #f8fafc; display: flex; overflow: hidden; }
.animate-fade-in { animation: layoutFade 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
@keyframes layoutFade { from { opacity: 0; transform: scale(0.99); } to { opacity: 1; transform: scale(1); } }

.custom-aside {
  width: 240px !important;
  min-width: 240px !important;   
  flex-shrink: 0 !important;     
  background-color: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 24px 14px;
  box-sizing: border-box;
}
.sidebar-brand { display: flex; align-items: center; gap: 12px; padding: 0 12px 24px 12px; border-bottom: 1px solid #f1f5f9; margin-bottom: 20px; }
.brand-logo { font-size: 28px; }
.brand-text h2 { margin: 0; font-size: 18px; font-weight: 800; color: #0f172a; letter-spacing: 0.5px; }
.brand-text p { margin: 2px 0 0 0; font-size: 11px; color: #94a3b8; font-weight: 500; }

.custom-menu { border-right: none !important; flex: 1; }
.menu-item-btn { height: 50px !important; line-height: 50px !important; margin-bottom: 8px !important; border-radius: 10px !important; font-size: 14px !important; font-weight: 500 !important; transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1) !important; color: #64748b !important; }
.menu-item-btn:hover { background-color: #f1f5f9 !important; color: #0f172a !important; transform: translateX(4px); }

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%) !important;
  color: #4f46e5 !important;
  font-weight: 700 !important;
}

.sidebar-user-panel { display: flex; align-items: center; gap: 12px; background-color: #f8fafc; padding: 12px; border-radius: 12px; border: 1px solid #e2e8f0; margin-top: auto; }
.user-avatar-mini { width: 38px; height: 38px; background: linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%); color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; box-shadow: 0 4px 10px rgba(79, 70, 229, 0.2); }
.user-meta-info { display: flex; flex-direction: column; gap: 4px; }
.username-text { font-size: 13px; font-weight: 600; color: #1e293b; }
.role-badge { font-size: 10px !important; padding: 0 6px !important; height: 18px !important; line-height: 16px !important; border: none !important; }

.right-main-container { display: flex; flex-direction: column; flex: 1; min-width: 0; }
.custom-header { background-color: #fff; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; }
.header-breadcrumb { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.crumb-parent { color: #94a3b8; font-weight: 500; }
.crumb-separator { color: #cbd5e1; }
.crumb-current { color: #1e293b; font-weight: 600; }
.logout-btn { border-radius: 8px !important; font-size: 13px !important; display: flex; align-items: center; gap: 4px; cursor: pointer; }
.custom-main { padding: 24px; box-sizing: border-box; overflow-y: auto; }
.main-content-card { min-height: calc(100vh - 112px); }
</style>