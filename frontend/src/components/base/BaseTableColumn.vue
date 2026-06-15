<template>
  <el-table-column v-bind="$attrs">
    <template v-for="(_, name) in $slots" #[name]="scope">
      <slot :name="name" v-bind="scope" />
    </template>
  </el-table-column>
</template>

<script setup>
defineOptions({
  inheritAttrs: false
})
</script>
