<template>
  <div class="base-table-actions">
    <el-button v-if="showEdit" type="primary" link @click="$emit('edit')">编辑</el-button>
    <el-button v-if="showDelete" type="danger" link :disabled="deleteDisabled" @click="$emit('delete')">删除</el-button>
    <slot />
  </div>
</template>

<script setup>
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
