<template>
  <el-form ref="formRef" :model="modelValue" :rules="rules" label-width="86px">
    <el-form-item label="名称" prop="name">
      <el-input v-model="modelValue.name" placeholder="请输入分类名称" />
    </el-form-item>
    <el-form-item label="图标" prop="icon">
      <div class="category-form__icon">
        <BaseImageUpload
          v-model="modelValue.icon"
          :preview-url="iconPreviewUrl"
          :max-size-mb="2"
          placeholder="上传图标"
          @uploaded="$emit('icon-uploaded', $event)"
        />
        <el-button v-if="modelValue.icon || iconPreviewUrl" text type="danger" @click="$emit('remove-icon')">移除图标</el-button>
      </div>
    </el-form-item>
    <el-form-item label="描述" prop="description">
      <el-input v-model="modelValue.description" type="textarea" :rows="3" placeholder="请输入描述" />
    </el-form-item>
    <el-form-item label="排序" prop="sort">
      <el-input-number v-model="modelValue.sort" :min="0" :max="999" />
    </el-form-item>
    <el-form-item label="状态" prop="status">
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
  iconPreviewUrl: {
    type: String,
    default: ''
  }
})

defineEmits(['icon-uploaded', 'remove-icon'])

const formRef = ref(null)
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

defineExpose({
  validate: () => formRef.value?.validate?.()
})
</script>

<style scoped>
.category-form__icon {
  display: flex;
  align-items: flex-end;
  gap: var(--spacing-sm);
}
</style>
