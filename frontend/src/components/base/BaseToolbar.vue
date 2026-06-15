<template>
  <BaseForm :inline="true" :model="model" class="base-toolbar">
    <slot />
    <BaseFormItem class="base-toolbar__actions">
      <BaseButton type="primary" @click="$emit('search')">
        <el-icon><Search /></el-icon>
        搜索
      </BaseButton>
      <BaseButton @click="$emit('reset')">
        <el-icon><Refresh /></el-icon>
        重置
      </BaseButton>
    </BaseFormItem>
  </BaseForm>
</template>

<script setup>
import { Search, Refresh } from '@element-plus/icons-vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseForm from '@/components/base/BaseForm.vue'
import BaseFormItem from '@/components/base/BaseFormItem.vue'

defineProps({
  model: {
    type: Object,
    required: true
  }
})

defineEmits(['search', 'reset'])
</script>

<style scoped>
.base-toolbar {
  margin-bottom: var(--spacing-lg);
}

.base-toolbar__actions {
  margin-right: 0;
}
</style>
