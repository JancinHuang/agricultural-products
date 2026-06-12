import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/views/Layout.vue'
import UserLayout from '@/views/UserLayout.vue'

const APP_TITLE = '助农商城'
const publicPaths = ['/login', '/register']

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/admin',
    component: Layout,
    redirect: '/admin/dashboard',
    meta: { requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页概览', requiresAdmin: true }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/User.vue'),
        meta: { title: '用户管理', requiresAdmin: true }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/Category.vue'),
        meta: { title: '分类管理', requiresAdmin: true }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/Product.vue'),
        meta: { title: '商品管理', requiresAdmin: true }
      },
      {
        path: 'banner',
        name: 'Banner',
        component: () => import('@/views/Banner.vue'),
        meta: { title: '轮播图管理', requiresAdmin: true }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/Order.vue'),
        meta: { title: '订单管理', requiresAdmin: true }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人信息', requiresAdmin: true }
      }
    ]
  },
  {
    path: '/',
    component: UserLayout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'shop',
        name: 'Shop',
        component: () => import('@/views/Shop.vue'),
        meta: { title: '商品浏览' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/Cart.vue'),
        meta: { title: '购物车' }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/ProductDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'my-order',
        name: 'MyOrder',
        component: () => import('@/views/MyOrder.vue'),
        meta: { title: '我的订单' }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/Checkout.vue'),
        meta: { title: '确认订单' }
      },
      {
        path: 'favorite',
        name: 'Favorite',
        component: () => import('@/views/Favorite.vue'),
        meta: { title: '我的收藏' }
      },
      {
        path: 'ai-assistant',
        name: 'AiAssistant',
        component: () => import('@/views/AiAssistant.vue'),
        meta: { title: 'AI购物助手' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人信息' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const clearAuth = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

const parseJwtPayload = (token) => {
  try {
    const base64 = token.split('.')[1]
    if (!base64) return null
    const normalized = base64.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(normalized)
        .split('')
        .map(char => `%${(`00${char.charCodeAt(0).toString(16)}`).slice(-2)}`)
        .join('')
    )
    return JSON.parse(json)
  } catch (error) {
    return null
  }
}

const isTokenExpired = (token) => {
  const payload = parseJwtPayload(token)
  if (!payload || !payload.exp) return true
  return payload.exp * 1000 <= Date.now()
}

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - ${APP_TITLE}` : APP_TITLE

  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  const isPublicPage = publicPaths.includes(to.path)

  if (!token) {
    if (isPublicPage) {
      next()
      return
    }
    next('/login')
    return
  }

  if (isTokenExpired(token)) {
    clearAuth()
    if (isPublicPage) {
      next()
      return
    }
    next('/login')
    return
  }

  if (user && user.username === 'admin' && !to.path.startsWith('/admin') && !isPublicPage) {
    next('/admin/dashboard')
    return
  }

  if (to.meta.requiresAdmin && (!user || user.username !== 'admin')) {
    next('/home')
    return
  }

  next()
})

export default router
