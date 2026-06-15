<template>
  <div class="layout-container">
    <el-container>
      <AdminSidebar
        :collapsed="isCollapse"
        :active-menu="activeMenu"
        @home="router.push('/admin/dashboard')"
      />
      <el-container>
        <AdminTopbar
          :collapsed="isCollapse"
          :title="currentTitle"
          :user-name="userName"
          :user-avatar="userAvatar"
          @toggle="toggleCollapse"
          @command="handleCommand"
        />
        <el-main class="layout-main">
          <router-view />
        </el-main>
        <el-footer class="layout-footer">
          <span>助农商城 &copy; 2026 | 管理后台</span>
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import AdminTopbar from '@/components/admin/AdminTopbar.vue'
import { confirmDanger } from '@/services/uiFeedback'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')
const userAvatar = computed(() => userStore.userInfo?.avatar || '')
const userName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '')

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command) => {
  if (command === 'logout') {
    confirmDanger('确定要退出登录吗？').then(() => {
      userStore.logout()
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/admin/profile')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

/* 内层 el-container 需要固定高度才能让 el-main 滚动 */
:deep(.el-container) {
  height: 100%;
}

.layout-main {
  background: var(--color-bg-page);
  padding: 20px;
  overflow-y: auto;
}

.layout-footer {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-top: 1px solid var(--color-border-light);
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}
</style>
