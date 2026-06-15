<template>
  <el-radio-group v-bind="$attrs" :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)">
    <slot />
  </el-radio-group>
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
