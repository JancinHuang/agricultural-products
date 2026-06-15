<template>
  <el-aside :width="collapsed ? '64px' : '220px'" class="layout-aside">
    <div class="logo" @click="$emit('home')">
      <el-icon :size="28"><Shop /></el-icon>
      <span v-show="!collapsed">助农商城</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      :collapse-transition="false"
      background-color="#1b3a26"
      text-color="#c8e6c9"
      active-text-color="#4caf50"
      router
    >
      <template v-for="group in menuGroups" :key="group.key">
        <el-menu-item v-if="!group.children" :index="group.path">
          <el-icon><component :is="iconMap[group.icon]" /></el-icon>
          <template #title>{{ group.label }}</template>
        </el-menu-item>
        <el-sub-menu v-else :index="group.key">
          <template #title>
            <el-icon><component :is="iconMap[group.icon]" /></el-icon>
            <span>{{ group.label }}</span>
          </template>
          <el-menu-item v-for="item in group.children" :key="item.path" :index="item.path">
            <el-icon><component :is="iconMap[item.icon]" /></el-icon>
            <template #title>{{ item.label }}</template>
          </el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>
  </el-aside>
</template>

<script setup>
import { DataAnalysis, Goods, Grid, List, Picture, Setting, Shop, User } from '@element-plus/icons-vue'
import { ADMIN_MENU_GROUPS } from '@/constants/navigation'

defineProps({
  collapsed: {
    type: Boolean,
    default: false
  },
  activeMenu: {
    type: String,
    required: true
  },
  menuGroups: {
    type: Array,
    default: () => ADMIN_MENU_GROUPS
  }
})

defineEmits(['home'])

const iconMap = {
  DataAnalysis,
  Goods,
  Grid,
  List,
  Picture,
  Setting,
  User
}
</script>

<style scoped>
.layout-aside {
  background: var(--color-sidebar-bg);
  transition: width 0.3s;
  overflow-y: auto;
  overflow-x: hidden;
  height: 100vh;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
  user-select: none;
}

.logo:hover {
  opacity: 0.8;
}

.logo span {
  white-space: nowrap;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-menu-item.is-active) {
  background-color: var(--color-sidebar-hover) !important;
}

:deep(.el-sub-menu .el-menu-item) {
  min-width: auto;
  padding-left: 50px !important;
}
</style>
