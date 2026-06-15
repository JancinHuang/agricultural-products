<template>
  <el-form-item v-bind="$attrs">
    <slot />
  </el-form-item>
</template>

<script setup>
defineOptions({
  inheritAttrs: false
})
</script>
