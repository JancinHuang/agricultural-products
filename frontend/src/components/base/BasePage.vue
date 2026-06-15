<template>
  <div class="base-page">
    <el-card class="base-page__card">
      <template v-if="$slots.header || title" #header>
        <div class="base-page__header">
          <div class="base-page__title">
            <slot name="title">{{ title }}</slot>
          </div>
          <div v-if="$slots.actions" class="base-page__actions">
            <slot name="actions" />
          </div>
        </div>
      </template>
      <slot />
    </el-card>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.base-page {
  padding: 0;
}

.base-page__card {
  border-radius: var(--radius-md);
}

.base-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-md);
}

.base-page__title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--color-text-primary);
}

.base-page__actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}
</style>
