<template>
  <el-radio v-bind="$attrs">
    <slot />
  </el-radio>
</template>

<script setup>
defineOptions({
  inheritAttrs: false
})
</script>
