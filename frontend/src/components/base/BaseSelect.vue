<template>
  <el-select v-bind="$attrs" :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)">
    <slot />
  </el-select>
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
