<template>
  <div class="auth-form-shell" :class="{ compact }">
    <div v-if="$slots.icon || title" class="form-header">
      <slot name="icon"></slot>
      <h1 v-if="compact">{{ title }}</h1>
      <h2 v-else>{{ title }}</h2>
      <p v-if="subtitle">{{ subtitle }}</p>
    </div>
    <slot></slot>
    <div v-if="$slots.footer" class="form-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  compact: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.auth-form-shell {
  width: 100%;
  max-width: 380px;
  padding: 20px 0;
}

.auth-form-shell.compact {
  width: 340px;
  max-width: none;
  padding: 32px 28px 24px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(20px);
  z-index: 1;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.compact .form-header {
  margin-bottom: 28px;
}

.form-header h1,
.form-header h2 {
  font-weight: 600;
  color: #1a1a1a;
}

.form-header h1 {
  font-size: 18px;
  letter-spacing: 2px;
}

.form-header h2 {
  font-size: 28px;
  margin-bottom: 10px;
}

.form-header p {
  color: #666;
  font-size: 14px;
}

.form-footer {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.compact .form-footer {
  display: flex;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
}

.form-footer :deep(a) {
  color: var(--color-primary);
  font-weight: 600;
  margin-left: 4px;
  text-decoration: none;
}

.compact .form-footer :deep(a) {
  margin-left: 0;
}

.form-footer :deep(a:hover) {
  text-decoration: underline;
}
</style>
