<template>
  <el-form ref="formRef" :model="modelValue" :rules="rules" label-width="80px">
    <el-form-item label="名称" prop="name">
      <el-input v-model="modelValue.name" placeholder="请输入产品名称" />
    </el-form-item>
    <el-form-item label="分类" prop="categoryId">
      <el-select v-model="modelValue.categoryId" placeholder="请选择分类" class="dialog-full">
        <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
    </el-form-item>
    <el-form-item label="价格" prop="price">
      <el-input-number v-model="modelValue.price" :min="0" :precision="2" class="dialog-full" />
    </el-form-item>
    <el-form-item label="库存" prop="stock">
      <el-input-number v-model="modelValue.stock" :min="0" class="dialog-full" />
    </el-form-item>
    <el-form-item label="单位" prop="unit">
      <el-input v-model="modelValue.unit" placeholder="如：斤、个、盒" />
    </el-form-item>
    <el-form-item label="产地" prop="origin">
      <el-input v-model="modelValue.origin" placeholder="请输入产地" />
    </el-form-item>
    <el-form-item label="图片" prop="image">
      <BaseImageUpload
        v-model="modelValue.image"
        :preview-url="imagePreviewUrl"
        placeholder="上传产品图"
        @uploaded="$emit('image-uploaded', $event)"
      />
    </el-form-item>
    <el-form-item label="描述" prop="description">
      <el-input v-model="modelValue.description" type="textarea" :rows="3" placeholder="请输入产品描述" />
    </el-form-item>
    <el-form-item label="状态" prop="status">
      <el-radio-group v-model="modelValue.status">
        <el-radio :value="1">上架</el-radio>
        <el-radio :value="0">下架</el-radio>
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
  categories: {
    type: Array,
    default: () => []
  },
  imagePreviewUrl: {
    type: String,
    default: ''
  }
})

defineEmits(['image-uploaded'])

const formRef = ref(null)

const rules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

defineExpose({
  validate: () => formRef.value?.validate?.()
})
</script>
