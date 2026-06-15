<template>
  <el-header class="layout-header">
    <div class="header-left">
      <el-icon class="collapse-btn" @click="$emit('toggle')">
        <Fold v-if="!collapsed" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <el-dropdown @command="$emit('command', $event)">
        <div class="user-info">
          <el-avatar :size="32" :src="userAvatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <span class="username">{{ userName }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人信息
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup>
import { ArrowDown, Expand, Fold, SwitchButton, User, UserFilled } from '@element-plus/icons-vue'

defineProps({
  collapsed: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  userName: {
    type: String,
    default: ''
  },
  userAvatar: {
    type: String,
    default: ''
  }
})

defineEmits(['toggle', 'command'])
</script>

<style scoped>
.layout-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 1;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #666;
}

.collapse-btn:hover {
  color: var(--color-primary);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 4px;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #333;
}
</style>
