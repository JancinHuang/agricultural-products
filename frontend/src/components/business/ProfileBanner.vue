<template>
  <div class="profile-banner">
    <div class="banner-inner">
      <el-avatar :size="80" :src="user?.avatar" class="banner-avatar">
        <el-icon :size="36"><UserFilled /></el-icon>
      </el-avatar>
      <div class="banner-info">
        <h2>{{ user?.nickname || user?.username }}</h2>
        <div class="banner-meta">
          <el-tag :type="user?.role === 1 ? 'danger' : ''" size="small" effect="dark">
            {{ user?.role === 1 ? '管理员' : '普通用户' }}
          </el-tag>
          <span class="join-time">
            <el-icon><Clock /></el-icon>
            {{ formatTime(user?.createTime) }} 加入
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Clock, UserFilled } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/time'

defineProps({
  user: {
    type: Object,
    default: null
  }
})
</script>

<style scoped>
.profile-banner {
  background: linear-gradient(135deg, #1b5e20 0%, #2e7d32 50%, #4caf50 100%);
  border-radius: var(--radius-lg);
  padding: 32px;
  margin-bottom: 24px;
  color: #fff;
}

.banner-inner {
  display: flex;
  align-items: center;
  gap: 24px;
}

.banner-avatar {
  border: 3px solid rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.banner-info h2 {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 8px;
  color: #fff;
}

.banner-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.join-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

@media (max-width: 768px) {
  .banner-inner {
    flex-direction: column;
    text-align: center;
  }

  .banner-meta {
    justify-content: center;
  }
}
</style>
