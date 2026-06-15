<template>
  <BaseToolbar :model="model" @search="$emit('search')" @reset="$emit('reset')">
    <slot />
  </BaseToolbar>
</template>

<script setup>
import BaseToolbar from '@/components/base/BaseToolbar.vue'

defineProps({
  model: {
    type: Object,
    required: true
  }
})

defineEmits(['search', 'reset'])
</script>
