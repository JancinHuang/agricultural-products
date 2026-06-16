<template>
  <div class="chat-header">
    <div class="chat-header__info">
      <div class="chat-header__icon">🌿</div>
      <div>
        <h2>AI 购物助手</h2>
        <p>智能推荐，帮你选好物</p>
      </div>
    </div>
    <BaseButton text :disabled="disabled" @click="$emit('clear')">
      <el-icon><Delete /></el-icon>
      清空对话
    </BaseButton>
  </div>
</template>

<script setup>
import { Delete } from '@element-plus/icons-vue'
import BaseButton from '@/components/base/BaseButton.vue'

defineProps({
  disabled: {
    type: Boolean,
    default: false
  }
})

defineEmits(['clear'])
</script>

<style scoped>
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-xl);
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: var(--color-white);
  flex-shrink: 0;
}

.chat-header__info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.chat-header__icon {
  font-size: 32px;
  line-height: 1;
}

.chat-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.chat-header p {
  margin: 2px 0 0;
  font-size: var(--font-size-xs);
  opacity: 0.85;
}

.chat-header .el-button {
  color: rgba(255, 255, 255, 0.85);
}

.chat-header .el-button:hover {
  color: var(--color-white);
}

@media (max-width: 768px) {
  .chat-header {
    padding: 12px var(--spacing-md);
  }
}
</style>
