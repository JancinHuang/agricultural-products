import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/views/Layout.vue'
import UserLayout from '@/views/UserLayout.vue'
import { clearAuth, getStoredUser, getToken, isAdminUser, isTokenExpired } from '@/utils/auth'

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

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - ${APP_TITLE}` : APP_TITLE

  const token = getToken()
  const user = getStoredUser()
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

  if (isAdminUser(user) && !to.path.startsWith('/admin') && !isPublicPage) {
    next('/admin/dashboard')
    return
  }

  if (to.meta.requiresAdmin && !isAdminUser(user)) {
    next('/home')
    return
  }

  next()
})

export default router
