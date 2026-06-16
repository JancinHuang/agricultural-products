<template>
  <el-checkbox v-bind="$attrs" :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)">
    <slot />
  </el-checkbox>
</template>

<script setup lang="ts">
defineOptions({
  inheritAttrs: false
})

defineProps<{
  modelValue?: string | number | boolean
}>()

defineEmits<{
  'update:modelValue': [value: string | number | boolean]
}>()
</script>
