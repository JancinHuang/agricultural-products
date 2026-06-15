<template>
  <el-form ref="formRef" :model="modelValue" :rules="rules" label-width="92px">
    <el-form-item label="标题" prop="title">
      <el-input v-model="modelValue.title" placeholder="请输入轮播标题" />
    </el-form-item>
    <el-form-item label="副标题">
      <el-input v-model="modelValue.subtitle" type="textarea" :rows="2" placeholder="请输入副标题" />
    </el-form-item>
    <el-form-item label="按钮文案">
      <el-input v-model="modelValue.buttonText" placeholder="例如：立即选购" />
    </el-form-item>
    <el-form-item label="跳转链接">
      <el-input v-model="modelValue.linkUrl" placeholder="/shop 或 /product/1" />
    </el-form-item>
    <el-form-item label="图片" prop="image">
      <BaseImageUpload
        v-model="modelValue.image"
        :preview-url="imagePreviewUrl"
        :width="220"
        :height="110"
        placeholder="上传轮播图"
        @uploaded="$emit('image-uploaded', $event)"
      />
    </el-form-item>
    <el-form-item label="排序">
      <el-input-number v-model="modelValue.sort" :min="0" :max="999" />
    </el-form-item>
    <el-form-item label="状态">
      <el-radio-group v-model="modelValue.status">
        <el-radio :value="1">启用</el-radio>
        <el-radio :value="0">禁用</el-radio>
      </el-radio-group>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref } from 'vue'
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
