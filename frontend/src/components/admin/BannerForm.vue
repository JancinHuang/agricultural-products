<template>
  <BaseForm ref="formRef" :model="modelValue" :rules="rules" label-width="92px">
    <BaseFormItem label="标题" prop="title">
      <BaseInput v-model="modelValue.title" placeholder="请输入轮播标题" />
    </BaseFormItem>
    <BaseFormItem label="副标题">
      <BaseInput v-model="modelValue.subtitle" type="textarea" :rows="2" placeholder="请输入副标题" />
    </BaseFormItem>
    <BaseFormItem label="按钮文案">
      <BaseInput v-model="modelValue.buttonText" placeholder="例如：立即选购" :disabled="modelValue.showButton === 0" />
    </BaseFormItem>
    <BaseFormItem label="跳转链接">
      <BaseInput v-model="modelValue.linkUrl" placeholder="/shop 或 /product/1" :disabled="modelValue.showButton === 0" />
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
    <BaseFormItem label="显示标题">
      <BaseSwitch v-model="modelValue.showTitle" :active-value="1" :inactive-value="0" />
    </BaseFormItem>
    <div v-if="modelValue.showTitle !== 0" class="banner-style-grid">
      <BaseFormItem label="标题颜色">
        <el-color-picker v-model="modelValue.titleColor" show-alpha />
      </BaseFormItem>
      <BaseFormItem label="标题字号">
        <BaseInputNumber v-model="modelValue.titleFontSize" :min="20" :max="72" />
      </BaseFormItem>
      <BaseFormItem label="标题粗细">
        <BaseSelect v-model="modelValue.titleFontWeight" placeholder="选择粗细">
          <BaseOption label="常规" :value="400" />
          <BaseOption label="中等" :value="500" />
          <BaseOption label="加粗" :value="700" />
          <BaseOption label="特粗" :value="800" />
        </BaseSelect>
      </BaseFormItem>
    </div>
    <div class="banner-style-grid">
      <BaseFormItem label="副标题颜色">
        <el-color-picker v-model="modelValue.subtitleColor" show-alpha />
      </BaseFormItem>
      <BaseFormItem label="副标题字号">
        <BaseInputNumber v-model="modelValue.subtitleFontSize" :min="12" :max="36" />
      </BaseFormItem>
    </div>
    <BaseFormItem label="显示按钮">
      <BaseSwitch v-model="modelValue.showButton" :active-value="1" :inactive-value="0" />
    </BaseFormItem>
    <BaseFormItem v-if="modelValue.showButton !== 0" label="按钮颜色">
      <el-color-picker v-model="modelValue.buttonColor" show-alpha />
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
import BaseOption from '@/components/base/BaseOption.vue'
import BaseRadio from '@/components/base/BaseRadio.vue'
import BaseRadioGroup from '@/components/base/BaseRadioGroup.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import BaseSwitch from '@/components/base/BaseSwitch.vue'
import BaseImageUpload from '@/components/base/BaseImageUpload.vue'

const props = defineProps({
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
  title: [
    {
      validator: (rule, value, callback) => {
        if (props.modelValue.showTitle !== 0 && !value) {
          callback(new Error('请输入标题'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  image: [{ required: true, message: '请上传图片', trigger: 'change' }]
}

defineExpose({
  validate: () => formRef.value?.validate?.()
})
</script>

<style scoped>
.banner-style-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: var(--spacing-md);
}
</style>
