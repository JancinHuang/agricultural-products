<template>
  <el-form ref="formRef" v-bind="$attrs">
    <slot />
  </el-form>
</template>

<script setup>
import { ref } from 'vue'

defineOptions({
  inheritAttrs: false
})

const formRef = ref(null)

defineExpose({
  validate: (...args) => formRef.value?.validate?.(...args),
  resetFields: (...args) => formRef.value?.resetFields?.(...args),
  clearValidate: (...args) => formRef.value?.clearValidate?.(...args),
  validateField: (...args) => formRef.value?.validateField?.(...args)
})
</script>
