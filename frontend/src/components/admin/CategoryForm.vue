<template>
  <BaseForm ref="formRef" :model="modelValue" :rules="rules" label-width="86px">
    <BaseFormItem label="名称" prop="name">
      <BaseInput v-model="modelValue.name" placeholder="请输入分类名称" />
    </BaseFormItem>
    <BaseFormItem label="图标" prop="icon">
      <div class="category-form__icon">
        <BaseImageUpload
          v-model="modelValue.icon"
          :preview-url="iconPreviewUrl"
          :max-size-mb="2"
          placeholder="上传图标"
          @uploaded="$emit('icon-uploaded', $event)"
        />
        <BaseButton v-if="modelValue.icon || iconPreviewUrl" text type="danger" @click="$emit('remove-icon')">移除图标</BaseButton>
      </div>
    </BaseFormItem>
    <BaseFormItem label="描述" prop="description">
      <BaseInput v-model="modelValue.description" type="textarea" :rows="3" placeholder="请输入描述" />
    </BaseFormItem>
    <BaseFormItem label="排序" prop="sort">
      <BaseInputNumber v-model="modelValue.sort" :min="0" :max="999" />
    </BaseFormItem>
    <BaseFormItem label="状态" prop="status">
      <BaseRadioGroup v-model="modelValue.status">
        <BaseRadio :value="1">启用</BaseRadio>
        <BaseRadio :value="0">禁用</BaseRadio>
      </BaseRadioGroup>
    </BaseFormItem>
  </BaseForm>
</template>

<script setup>
import { ref } from 'vue'
import BaseButton from '@/components/base/BaseButton.vue'
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
