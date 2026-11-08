import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import Home from '../views/Home.vue'
import Recognize from '../views/Index.vue'
import Record from '../views/Record.vue'
import Statistics from '../views/Statistics.vue'
import Disease from '../views/Disease.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/home', // 登录后默认跳首页
    children: [
      {
        path: 'home',
        name: 'Home',
        component: Home,
        meta: { requiresAuth: true, title: '数据概览' }
      },
      {
        path: 'recognize',
        name: 'Recognize',
        component: Recognize,
        meta: { requiresAuth: true, title: '病害识别' }
      },
      {
        path: 'record',
        name: 'Record',
        component: Record,
        meta: { requiresAuth: true, title: '历史记录' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: Statistics,
        meta: { requiresAuth: true, title: '数据统计' }
      },
      {
        path: 'disease',
        name: 'Disease',
        component: Disease,
        meta: { requiresAuth: true, title: '病害知识库' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    if (to.meta.title) {
      document.title = to.meta.title + ' - 植物叶片病害识别与防治系统'
    }
    next()
  }
})

export default router
