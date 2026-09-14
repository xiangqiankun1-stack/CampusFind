import { createRouter, createWebHistory } from 'vue-router'

// 1. 静态引入核心外壳和所有子组件
import Layout from '../views/Layout.vue'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue' // 🌟 核心修复：引入你写好的注册页面组件！
import Add from '../views/Add.vue'
import Admin from '../views/Admin.vue'           // 对应你的发布审核组件
import ClaimAdmin from '../views/ClaimAdmin.vue' // 对应你的认领流转审核组件
import My from '../views/My.vue'                 // 完美对接你刚刚上传的“我的个人认领申请记录”组件
import MyPublish from '../views/MyPublish.vue'
import MyPendingClaims from '../views/MyPendingClaims.vue'

const routes = [
  // ================= 根路径：默认引导至系统的独立登录页 =================
  {
    path: '/',
    component: Login
  },
  
  // ================= 🌟 核心修复：注册页面路由定义 =================
  {
    path: '/register',
    component: Register
  },
  
  // ================= 系统主骨架路由（挂载左侧全权限导航侧边栏） =================
  {
    path: '/main',
    component: Layout,
    redirect: '/home', // 默认重定向到数据大厅
    children: [
      { 
        path: '/home', 
        component: Home 
      },
      { 
        path: '/add', 
        component: Add 
      },
      { 
        path: '/pending', 
        component: Admin       // 将 /pending 路径映射到 Admin.vue 组件
      },
  /*     { 
        path: '/claims', 
        component: ClaimAdmin  // 将 /claims 路径映射到 ClaimAdmin.vue 组件
      }, */
      { 
        path: '/my-claims', 
        component: My          // 正式将 /my-claims 路径绑定到你的 My.vue 组件上！
      },
      {
        path: '/my-publish',
        component: MyPublish
      },
      {
        path: '/my-pending-claims',
        component: MyPendingClaims
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router