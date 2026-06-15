<template>
  <header class="user-header">
    <div class="header-inner">
      <div class="header-logo" @click="$emit('home')">
        <el-icon :size="24"><Shop /></el-icon>
        <span>助农商城</span>
      </div>

      <nav class="header-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          :class="{ active: routePath === item.path }"
        >
          {{ item.label }}
        </router-link>
      </nav>

      <div class="header-right">
        <div class="header-search">
          <el-input
            v-model="keyword"
            placeholder="搜索商品..."
            :prefix-icon="Search"
            clearable
            @keyup.enter="$emit('search')"
          />
        </div>

        <div class="header-cart" @click="$emit('cart')">
          <el-badge :value="cartCount" :hidden="cartCount === 0" :max="99">
            <el-icon :size="22"><ShoppingCart /></el-icon>
          </el-badge>
        </div>

        <el-dropdown @command="$emit('command', $event)">
          <div class="header-user">
            <el-avatar :size="30" :src="user?.avatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <span class="username">{{ user?.nickname || user?.username }}</span>
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
</template>

<script setup>
import {
  ArrowDown,
  List,
  Search,
  Shop,
  ShoppingCart,
  Star,
  SwitchButton,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import { USER_NAV_ITEMS } from '@/constants/navigation'

defineProps({
  routePath: {
    type: String,
    required: true
  },
  user: {
    type: Object,
    default: null
  },
  cartCount: {
    type: Number,
    default: 0
  },
  navItems: {
    type: Array,
    default: () => USER_NAV_ITEMS
  }
})

defineEmits(['home', 'cart', 'search', 'command'])

const keyword = defineModel('keyword', {
  type: String,
  default: ''
})
</script>

<style scoped>
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

.header-nav a:hover,
.header-nav a.active {
  color: var(--color-primary);
  background: var(--color-primary-bg);
}

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

.header-search :deep(.el-input__wrapper:hover),
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
}
</style>
