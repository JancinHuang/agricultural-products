<template>
  <div class="page-header">
    <div>
      <p class="eyebrow">{{ eyebrow }}</p>
      <h2>{{ title }}</h2>
    </div>
    <el-button type="primary" plain @click="$emit('shop')">继续选购</el-button>
  </div>
</template>

<script setup>
defineProps({
  eyebrow: {
    type: String,
    default: 'Order Center'
  },
  title: {
    type: String,
    default: '我的订单'
  }
})

defineEmits(['shop'])
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
}

.eyebrow {
  margin: 0 0 4px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.page-header h2 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 24px;
  font-weight: 700;
}
</style>
