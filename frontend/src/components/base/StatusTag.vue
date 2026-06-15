<template>
  <el-tag :type="option.type">{{ option.label }}</el-tag>
</template>

<script setup>
import { computed } from 'vue'
import { ENABLE_STATUS_OPTIONS } from '@/constants/status'

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
