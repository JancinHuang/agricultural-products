<template>
  <el-option v-bind="$attrs">
    <slot />
  </el-option>
</template>

<script setup>
defineOptions({
  inheritAttrs: false
})
</script>
