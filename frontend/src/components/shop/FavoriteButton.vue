<template>
  <BaseButton :type="active ? 'danger' : 'default'" :size="size" @click="$emit('toggle')">
    <el-icon><Star /></el-icon>
    {{ active ? activeText : inactiveText }}
  </BaseButton>
</template>

<script setup>
import { Star } from '@element-plus/icons-vue'
import BaseButton from '@/components/base/BaseButton.vue'

defineProps({
  active: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default'
  },
  activeText: {
    type: String,
    default: '已收藏'
  },
  inactiveText: {
    type: String,
    default: '收藏'
  }
})

defineEmits(['toggle'])
</script>
