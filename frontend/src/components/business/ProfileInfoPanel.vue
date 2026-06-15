<template>
  <div class="info-panel">
    <div class="panel-header">
      <h3>基本信息</h3>
    </div>
    <div class="info-list">
      <div v-for="item in infoItems" :key="item.label" class="info-item">
        <div class="info-icon">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="info-detail">
          <span class="info-label">{{ item.label }}</span>
          <span class="info-value">{{ item.value }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Message, Phone, User } from '@element-plus/icons-vue'

const props = defineProps({
  user: {
    type: Object,
    default: null
  }
})

const infoItems = computed(() => [
  { label: '用户名', value: props.user?.username || '-', icon: User },
  { label: '手机号', value: props.user?.phone || '未设置', icon: Phone },
  { label: '邮箱', value: props.user?.email || '未设置', icon: Message }
])
</script>

<style scoped>
.info-panel {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border-light);
}

.panel-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: var(--color-text-primary);
}

.info-list {
  padding: 8px 0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 20px;
  transition: background var(--transition-fast);
}

.info-item:hover {
  background: var(--color-bg-section);
}

.info-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-primary-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  flex-shrink: 0;
}

.info-detail {
  flex: 1;
  min-width: 0;
}

.info-label {
  display: block;
  font-size: 12px;
  color: var(--color-text-placeholder);
  margin-bottom: 2px;
}

.info-value {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
