<template>
  <el-form :inline="true" :model="model" class="base-toolbar">
    <slot />
    <el-form-item class="base-toolbar__actions">
      <el-button type="primary" @click="$emit('search')">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button @click="$emit('reset')">
        <el-icon><Refresh /></el-icon>
        重置
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { Search, Refresh } from '@element-plus/icons-vue'

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
