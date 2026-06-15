<template>
  <el-row :gutter="20" class="dashboard-stat-row">
    <el-col :span="6" v-for="item in items" :key="item.key">
      <el-card class="dashboard-stat-card" :body-style="{ padding: '20px' }">
        <div class="dashboard-stat-card__content">
          <div class="dashboard-stat-card__icon" :style="{ background: item.color }">
            <el-icon :size="28"><component :is="item.icon" /></el-icon>
          </div>
          <div class="dashboard-stat-card__info">
            <div class="dashboard-stat-card__value">{{ item.value }}</div>
            <div class="dashboard-stat-card__label">{{ item.label }}</div>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
defineProps({
  items: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.dashboard-stat-row {
  margin-bottom: var(--spacing-lg);
}

.dashboard-stat-card {
  cursor: pointer;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
}

.dashboard-stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.dashboard-stat-card__content {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.dashboard-stat-card__icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-white);
  border-radius: 12px;
}

.dashboard-stat-card__info {
  flex: 1;
}

.dashboard-stat-card__value {
  color: var(--color-text-primary);
  font-size: 28px;
  font-weight: 600;
}

.dashboard-stat-card__label {
  margin-top: 4px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}
</style>
