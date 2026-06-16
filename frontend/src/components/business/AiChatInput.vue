<template>
  <div class="chat-input-area">
    <div class="chat-input-area__wrapper">
      <BaseInput
        :model-value="modelValue"
        type="textarea"
        :rows="2"
        placeholder="输入你想了解的商品或需求，按 Enter 发送..."
        resize="none"
        @update:model-value="$emit('update:modelValue', $event)"
        @keydown.enter.exact.prevent="$emit('send')"
      />
      <BaseButton
        type="primary"
        :loading="waiting || typing"
        :disabled="!modelValue.trim() || waiting || typing"
        @click="$emit('send')"
      >
        <el-icon v-if="!waiting && !typing"><Promotion /></el-icon>
        {{ waiting ? '思考中...' : typing ? '回复中...' : '发送' }}
      </BaseButton>
    </div>
  </div>
</template>

<script setup>
import { Promotion } from '@element-plus/icons-vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseInput from '@/components/base/BaseInput.vue'

defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  waiting: {
    type: Boolean,
    default: false
  },
  typing: {
    type: Boolean,
    default: false
  }
})

defineEmits(['update:modelValue', 'send'])
</script>

<style scoped>
.chat-input-area {
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--color-white);
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}

.chat-input-area__wrapper {
  display: flex;
  gap: var(--spacing-md);
  align-items: flex-end;
}

.chat-input-area__wrapper :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
}

.chat-input-area__wrapper :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.chat-input-area__wrapper .el-button {
  height: 64px;
  padding: 0 var(--spacing-lg);
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .chat-input-area {
    padding: 12px var(--spacing-md);
  }

  .chat-input-area__wrapper .el-button {
    height: 56px;
    padding: 0 var(--spacing-md);
  }
}
</style>
