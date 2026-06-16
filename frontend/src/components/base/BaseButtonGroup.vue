<template>
  <el-button-group v-bind="$attrs">
    <slot />
  </el-button-group>
</template>

<script setup lang="ts">
defineOptions({
  inheritAttrs: false
})
</script>
