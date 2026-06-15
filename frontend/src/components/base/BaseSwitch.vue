<template>
  <el-switch v-bind="$attrs" :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" />
</template>

<script setup>
defineOptions({
  inheritAttrs: false
})

defineProps({
  modelValue: {
    default: undefined
  }
})

defineEmits(['update:modelValue'])
</script>
