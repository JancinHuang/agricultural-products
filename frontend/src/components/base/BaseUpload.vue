<template>
  <el-upload v-bind="$attrs">
    <slot />
    <template v-if="$slots.file" #file="scope">
      <slot name="file" v-bind="scope" />
    </template>
    <template v-if="$slots.tip" #tip>
      <slot name="tip" />
    </template>
  </el-upload>
</template>

<script setup lang="ts">
defineOptions({
  inheritAttrs: false
})
</script>
