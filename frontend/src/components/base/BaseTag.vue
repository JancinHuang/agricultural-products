<template>
  <el-tag v-bind="$attrs">
    <slot />
  </el-tag>
</template>

<script setup>
defineOptions({
  inheritAttrs: false
})
</script>
