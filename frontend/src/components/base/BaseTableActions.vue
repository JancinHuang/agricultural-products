<template>
  <div class="base-table-actions">
    <BaseButton v-if="showEdit" type="primary" link @click="$emit('edit')">编辑</BaseButton>
    <BaseButton v-if="showDelete" type="danger" link :disabled="deleteDisabled" @click="$emit('delete')">删除</BaseButton>
    <slot />
  </div>
</template>

<script setup>
import BaseButton from '@/components/base/BaseButton.vue'

defineProps({
  showEdit: {
    type: Boolean,
    default: true
  },
  showDelete: {
    type: Boolean,
    default: true
  },
  deleteDisabled: {
    type: Boolean,
    default: false
  }
})

defineEmits(['edit', 'delete'])
</script>

<style scoped>
.base-table-actions {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
</style>
