<template>
  <el-upload
    class="base-image-upload"
    :show-file-list="false"
    :before-upload="beforeUpload"
    :http-request="handleUpload"
    :accept="accept"
  >
    <div class="base-image-upload__trigger" :style="{ width: `${width}px`, height: `${height}px` }">
      <img v-if="displayUrl" :src="displayUrl" class="base-image-upload__preview" />
      <div v-else class="base-image-upload__placeholder">
        <el-icon><Plus /></el-icon>
        <span>{{ placeholder }}</span>
      </div>
    </div>
  </el-upload>
</template>

<script setup>
import { computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ALLOWED_IMAGE_ACCEPT } from '@/utils/uploadValidation'
import { imageUtils } from '@/utils/imageUtils'
import { useImageUpload } from '@/composables/useImageUpload'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  previewUrl: {
    type: String,
    default: ''
  },
  width: {
    type: Number,
    default: 110
  },
  height: {
    type: Number,
    default: 110
  },
  maxSizeMb: {
    type: Number,
    default: 5
  },
  placeholder: {
    type: String,
    default: '上传图片'
  },
  accept: {
    type: String,
    default: ALLOWED_IMAGE_ACCEPT
  }
})

const emit = defineEmits(['update:modelValue', 'uploaded', 'error'])

const { previewUrl: localPreviewUrl, beforeUpload, upload } = useImageUpload({
  maxSizeMb: props.maxSizeMb,
  sizeMessage: `图片不能超过 ${props.maxSizeMb}MB`
})

const displayUrl = computed(() => {
  if (props.previewUrl) return props.previewUrl
  if (localPreviewUrl.value) return localPreviewUrl.value
  return props.modelValue ? imageUtils.getImageUrl(props.modelValue) : ''
})

const handleUpload = async (options) => {
  try {
    const result = await upload(options.file)
    emit('update:modelValue', result.objectKey)
    emit('uploaded', result)
    options.onSuccess?.(result)
  } catch (error) {
    console.error(error)
    emit('error', error)
    options.onError?.(error)
  }
}
</script>

<style scoped>
.base-image-upload__trigger {
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  background: var(--color-bg-section);
  transition: border-color var(--transition-fast), background var(--transition-fast);
}

.base-image-upload__trigger:hover {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
}

.base-image-upload__preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.base-image-upload__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--color-text-placeholder);
  font-size: var(--font-size-xs);
}
</style>
