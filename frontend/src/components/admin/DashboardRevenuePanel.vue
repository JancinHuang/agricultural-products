<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>营收统计</span>
      </div>
    </template>
    <div class="dashboard-revenue">
      <div v-for="item in items" :key="item.label" class="dashboard-revenue__item">
        <div class="dashboard-revenue__label">{{ item.label }}</div>
        <div class="dashboard-revenue__value">{{ item.value }}</div>
      </div>
    </div>
  </el-card>
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
.dashboard-revenue {
  height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  padding: var(--spacing-lg);
}

.dashboard-revenue__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg);
  background: var(--color-bg-section);
  border-radius: var(--radius-md);
}

.dashboard-revenue__label {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.dashboard-revenue__value {
  color: var(--color-primary);
  font-size: 24px;
  font-weight: 600;
}
</style>
