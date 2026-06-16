<template>
  <el-rate v-bind="$attrs" :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" />
</template>

<script setup lang="ts">
defineOptions({
  inheritAttrs: false
})

defineProps<{
  modelValue?: number
}>()

defineEmits<{
  'update:modelValue': [value: number]
}>()
</script>
