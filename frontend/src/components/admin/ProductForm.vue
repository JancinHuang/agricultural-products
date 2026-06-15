<template>
  <BaseForm ref="formRef" :model="modelValue" :rules="rules" label-width="80px">
    <BaseFormItem label="名称" prop="name">
      <BaseInput v-model="modelValue.name" placeholder="请输入产品名称" />
    </BaseFormItem>
    <BaseFormItem label="分类" prop="categoryId">
      <BaseSelect v-model="modelValue.categoryId" placeholder="请选择分类" class="dialog-full">
        <BaseOption v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
      </BaseSelect>
    </BaseFormItem>
    <BaseFormItem label="价格" prop="price">
      <BaseInputNumber v-model="modelValue.price" :min="0" :precision="2" class="dialog-full" />
    </BaseFormItem>
    <BaseFormItem label="库存" prop="stock">
      <BaseInputNumber v-model="modelValue.stock" :min="0" class="dialog-full" />
    </BaseFormItem>
    <BaseFormItem label="单位" prop="unit">
      <BaseInput v-model="modelValue.unit" placeholder="如：斤、个、盒" />
    </BaseFormItem>
    <BaseFormItem label="产地" prop="origin">
      <BaseInput v-model="modelValue.origin" placeholder="请输入产地" />
    </BaseFormItem>
    <BaseFormItem label="图片" prop="image">
      <BaseImageUpload
        v-model="modelValue.image"
        :preview-url="imagePreviewUrl"
        placeholder="上传产品图"
        @uploaded="$emit('image-uploaded', $event)"
      />
    </BaseFormItem>
    <BaseFormItem label="描述" prop="description">
      <BaseInput v-model="modelValue.description" type="textarea" :rows="3" placeholder="请输入产品描述" />
    </BaseFormItem>
    <BaseFormItem label="状态" prop="status">
      <BaseRadioGroup v-model="modelValue.status">
        <BaseRadio :value="1">上架</BaseRadio>
        <BaseRadio :value="0">下架</BaseRadio>
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
import BaseOption from '@/components/base/BaseOption.vue'
import BaseRadio from '@/components/base/BaseRadio.vue'
import BaseRadioGroup from '@/components/base/BaseRadioGroup.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
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
