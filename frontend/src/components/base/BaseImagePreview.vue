<template>
  <el-dialog v-model="innerVisible" :title="title" :width="width" append-to-body>
    <img v-if="src" :src="src" class="base-image-preview__image" />
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  src: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: '图片预览'
  },
  width: {
    type: String,
    default: '520px'
  }
})

const emit = defineEmits(['update:modelValue'])

const innerVisible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})
</script>

<style scoped>
.base-image-preview__image {
  width: 100%;
  display: block;
  border-radius: var(--radius-sm);
}
</style>
