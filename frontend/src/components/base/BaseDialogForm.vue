<template>
  <el-dialog
    v-model="innerVisible"
    :title="title"
    :width="width"
    :close-on-click-modal="closeOnClickModal"
    @closed="$emit('closed')"
  >
    <slot />
    <template #footer>
      <slot name="footer">
        <el-button @click="innerVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="$emit('submit')">确定</el-button>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  width: {
    type: String,
    default: '560px'
  },
  loading: {
    type: Boolean,
    default: false
  },
  closeOnClickModal: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'submit', 'closed'])

const innerVisible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})
</script>
