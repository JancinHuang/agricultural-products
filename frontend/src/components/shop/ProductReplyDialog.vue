<template>
  <el-dialog v-model="innerVisible" title="回复评价" width="460px" append-to-body>
    <el-input
      v-model="form.content"
      type="textarea"
      :rows="4"
      maxlength="500"
      show-word-limit
      placeholder="请输入回复内容"
    />
    <template #footer>
      <el-button @click="innerVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="$emit('submit')">提交</el-button>
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
  form: {
    type: Object,
    required: true
  },
  submitting: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const innerVisible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})
</script>
