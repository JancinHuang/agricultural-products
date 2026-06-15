<template>
  <BaseForm ref="formRef" :model="modelValue" :rules="rules" label-width="92px">
    <BaseFormItem label="标题" prop="title">
      <BaseInput v-model="modelValue.title" placeholder="请输入轮播标题" />
    </BaseFormItem>
    <BaseFormItem label="副标题">
      <BaseInput v-model="modelValue.subtitle" type="textarea" :rows="2" placeholder="请输入副标题" />
    </BaseFormItem>
    <BaseFormItem label="按钮文案">
      <BaseInput v-model="modelValue.buttonText" placeholder="例如：立即选购" />
    </BaseFormItem>
    <BaseFormItem label="跳转链接">
      <BaseInput v-model="modelValue.linkUrl" placeholder="/shop 或 /product/1" />
    </BaseFormItem>
    <BaseFormItem label="图片" prop="image">
      <BaseImageUpload
        v-model="modelValue.image"
        :preview-url="imagePreviewUrl"
        :width="220"
        :height="110"
        placeholder="上传轮播图"
        @uploaded="$emit('image-uploaded', $event)"
      />
    </BaseFormItem>
    <BaseFormItem label="排序">
      <BaseInputNumber v-model="modelValue.sort" :min="0" :max="999" />
    </BaseFormItem>
    <BaseFormItem label="状态">
      <BaseRadioGroup v-model="modelValue.status">
        <BaseRadio :value="1">启用</BaseRadio>
        <BaseRadio :value="0">禁用</BaseRadio>
      </BaseRadioGroup>
    </BaseFormItem>
  </BaseForm>
</template>

<script setup>
import { ref } from 'vue'
import BaseForm from '@/components/base/BaseForm.vue'
import BaseFormItem from '@/components/base/BaseFormItem.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseInputNumber from '@/components/base/BaseInputNumber.vue'
import BaseRadio from '@/components/base/BaseRadio.vue'
import BaseRadioGroup from '@/components/base/BaseRadioGroup.vue'
import BaseImageUpload from '@/components/base/BaseImageUpload.vue'

defineProps({
  modelValue: {
    type: Object,
    required: true
  },
  imagePreviewUrl: {
    type: String,
    default: ''
  }
})

defineEmits(['image-uploaded'])

const formRef = ref(null)
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  image: [{ required: true, message: '请上传图片', trigger: 'change' }]
}

defineExpose({
  validate: () => formRef.value?.validate?.()
})
</script>
