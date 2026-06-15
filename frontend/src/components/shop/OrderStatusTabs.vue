<template>
  <div class="status-tabs">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      :class="['tab-item', { active: modelValue === tab.key }]"
      type="button"
      @click="$emit('update:modelValue', tab.key)"
    >
      <span>{{ tab.label }}</span>
      <em>{{ counts[tab.key] || 0 }}</em>
    </button>
  </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    required: true
  },
  tabs: {
    type: Array,
    required: true
  },
  counts: {
    type: Object,
    default: () => ({})
  }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.status-tabs {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-sm);
  background: var(--color-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.tab-item {
  height: 36px;
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 0 18px;
  border: 0;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: var(--font-size-sm);
}

.tab-item em {
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  border-radius: 999px;
  background: var(--color-bg-light);
  color: var(--color-primary);
  font-size: var(--font-size-xs);
  font-style: normal;
}

.tab-item.active {
  background: var(--color-primary);
  color: var(--color-white);
}

.tab-item.active em {
  background: rgba(255, 255, 255, 0.2);
  color: var(--color-white);
}

@media (max-width: 900px) {
  .status-tabs {
    overflow-x: auto;
  }
}
</style>
