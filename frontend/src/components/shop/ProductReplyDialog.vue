<template>
  <BaseDialog v-model="innerVisible" title="回复评价" width="460px" append-to-body>
    <BaseInput
      v-model="form.content"
      type="textarea"
      :rows="4"
      maxlength="500"
      show-word-limit
      placeholder="请输入回复内容"
    />
    <template #footer>
      <BaseButton @click="innerVisible = false">取消</BaseButton>
      <BaseButton type="primary" :loading="submitting" @click="$emit('submit')">提交</BaseButton>
    </template>
  </BaseDialog>
</template>

<script setup>
import { computed } from 'vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseDialog from '@/components/base/BaseDialog.vue'
import BaseInput from '@/components/base/BaseInput.vue'

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
