<template>
  <BaseTag :type="option.type">{{ option.label }}</BaseTag>
</template>

<script setup>
import { computed } from 'vue'
import { ENABLE_STATUS_OPTIONS } from '@/constants/status'
import BaseTag from '@/components/base/BaseTag.vue'

const props = defineProps({
  value: {
    type: [Number, String, Boolean],
    required: true
  },
  options: {
    type: Array,
    default: () => ENABLE_STATUS_OPTIONS
  }
})

const option = computed(() => props.options.find(item => item.value === props.value) || {
  label: String(props.value),
  type: 'info'
})
</script>
