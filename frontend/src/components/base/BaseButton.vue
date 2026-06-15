<template>
  <el-button v-bind="$attrs" @click="$emit('click', $event)">
    <slot />
  </el-button>
</template>

<script setup>
defineOptions({
  inheritAttrs: false
})

defineEmits(['click'])
</script>
