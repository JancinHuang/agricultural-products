<template>
  <div class="user-layout">
    <header class="user-header">
      <div class="header-inner">
        <div class="header-logo" @click="router.push('/home')">
          <el-icon :size="24"><Shop /></el-icon>
          <span>助农商城</span>
        </div>

        <nav class="header-nav">
          <router-link to="/home" :class="{ active: route.path === '/home' }">首页</router-link>
          <router-link to="/shop" :class="{ active: route.path === '/shop' }">全部商品</router-link>
          <router-link to="/my-order" :class="{ active: route.path === '/my-order' }">我的订单</router-link>
          <router-link to="/favorite" :class="{ active: route.path === '/favorite' }">我的收藏</router-link>
          <router-link to="/ai-assistant" :class="{ active: route.path === '/ai-assistant' }">AI助手</router-link>
        </nav>

        <div class="header-right">
          <div class="header-search">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品..."
              :prefix-icon="Search"
              clearable
              @keyup.enter="handleSearch"
            />
          </div>

          <div class="header-cart" @click="router.push('/cart')">
            <el-badge :value="cartStore.selectedCount" :hidden="cartStore.selectedCount === 0" :max="99">
              <el-icon :size="22"><ShoppingCart /></el-icon>
            </el-badge>
          </div>

          <el-dropdown @command="handleCommand">
            <div class="header-user">
              <el-avatar :size="30" :src="userStore.userInfo?.avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="orders">
                  <el-icon><List /></el-icon>
                  我的订单
                </el-dropdown-item>
                <el-dropdown-item command="favorites">
                  <el-icon><Star /></el-icon>
                  我的收藏
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="user-main">
      <router-view />
    </main>

    <footer class="user-footer">
      <div class="footer-inner">
        <div class="footer-links">
          <router-link to="/shop">全部商品</router-link>
          <router-link to="/my-order">我的订单</router-link>
          <router-link to="/favorite">我的收藏</router-link>
          <router-link to="/ai-assistant">AI助手</router-link>
          <router-link to="/profile">个人信息</router-link>
        </div>
        <p class="footer-copy">助农商城 &copy; 2026 | 助力乡村，品质溯源</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Shop, Search, ShoppingCart, User, UserFilled, ArrowDown,
  SwitchButton, List, Star
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const searchKeyword = ref('')

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    router.push({ path: '/shop', query: { keyword } })
  } else {
    router.push('/shop')
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'orders') {
    router.push('/my-order')
  } else if (command === 'favorites') {
    router.push('/favorite')
  }
}

onMounted(() => {
  cartStore.init()
})
</script>

<style scoped>
.user-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--color-bg-page);
}

/* ── Header ── */
.user-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  height: 64px;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  height: 100%;
  gap: 32px;
}

/* Logo */
.header-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-primary);
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
  transition: opacity var(--transition-fast);
}

.header-logo:hover {
  opacity: 0.8;
}

/* Nav */
.header-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-nav a {
  padding: 6px 16px;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-secondary);
  text-decoration: none;
  border-radius: var(--radius-sm);
  transition: color var(--transition-fast), background var(--transition-fast);
}

.header-nav a:hover {
  color: var(--color-primary);
  background: var(--color-primary-bg);
}

.header-nav a.active {
  color: var(--color-primary);
  background: var(--color-primary-bg);
}

/* Right section */
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-left: auto;
}

.header-search :deep(.el-input__wrapper) {
  border-radius: 20px;
  box-shadow: 0 0 0 1px var(--color-border-light) inset;
  width: 200px;
}

.header-search :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.header-search :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.header-cart {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: color var(--transition-fast);
  padding: 4px;
}

.header-cart:hover {
  color: var(--color-primary);
}

.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.header-user:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: var(--color-text-primary);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-icon {
  font-size: 12px;
  color: var(--color-text-placeholder);
}

/* ── Main ── */
.user-main {
  flex: 1;
  overflow-y: auto;
}

/* ── Footer ── */
.user-footer {
  background: var(--color-sidebar-bg);
  color: rgba(255, 255, 255, 0.6);
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 24px;
  text-align: center;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 16px;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-size: 13px;
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: #fff;
}

.footer-copy {
  font-size: 13px;
  margin: 0;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .header-inner {
    gap: 12px;
    padding: 0 12px;
  }

  .header-nav {
    display: none;
  }

  .header-search :deep(.el-input__wrapper) {
    width: 140px;
  }

  .username {
    display: none;
  }

  .footer-links {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
