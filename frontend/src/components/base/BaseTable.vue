<template>
  <el-table v-bind="$attrs">
    <slot />
  </el-table>
</template>

<script setup>
defineOptions({
  inheritAttrs: false
})
</script>
