<template>
  <el-dialog v-bind="$attrs" :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)">
    <slot />
    <template v-if="$slots.footer" #footer>
      <slot name="footer" />
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
defineOptions({
  inheritAttrs: false
})

defineProps<{
  modelValue?: boolean
}>()

defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>
