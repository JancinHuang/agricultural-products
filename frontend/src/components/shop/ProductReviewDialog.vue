<template>
  <BaseDialog
    v-model="innerVisible"
    :title="isEditMode ? '编辑评价' : '发表评价'"
    width="520px"
    append-to-body
  >
    <BaseForm label-width="80px">
      <BaseFormItem label="评分">
        <BaseRate v-model="form.rating" />
      </BaseFormItem>
      <BaseFormItem label="评价内容">
        <BaseInput
          v-model="form.content"
          type="textarea"
          :rows="5"
          maxlength="500"
          show-word-limit
          placeholder="说说商品质量、包装、口感或使用体验"
        />
      </BaseFormItem>
      <BaseFormItem label="图片">
        <BaseUpload
          v-model:file-list="imageList"
          class="review-uploader"
          :action="uploadAction"
          list-type="picture-card"
          :headers="uploadHeaders"
          :before-upload="beforeUpload"
          :on-success="onUploadSuccess"
          :on-error="onUploadError"
          :on-remove="onUploadRemove"
          :on-preview="onPreview"
          :accept="accept"
          :limit="5"
        >
          <el-icon><Plus /></el-icon>
        </BaseUpload>
        <div class="upload-tip">最多上传 5 张图片</div>
      </BaseFormItem>
    </BaseForm>
    <template #footer>
      <BaseButton @click="$emit('cancel')">取消</BaseButton>
      <BaseButton type="primary" :loading="submitting" @click="$emit('submit')">提交</BaseButton>
    </template>
  </BaseDialog>
</template>

<script setup>
import { computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseDialog from '@/components/base/BaseDialog.vue'
import BaseForm from '@/components/base/BaseForm.vue'
import BaseFormItem from '@/components/base/BaseFormItem.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseRate from '@/components/base/BaseRate.vue'
import BaseUpload from '@/components/base/BaseUpload.vue'
import { UPLOAD_ACTION_URL } from '@/constants/endpoints'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  isEditMode: {
    type: Boolean,
    default: false
  },
  form: {
    type: Object,
    required: true
  },
  imageList: {
    type: Array,
    default: () => []
  },
  uploadHeaders: {
    type: Object,
    default: () => ({})
  },
  uploadAction: {
    type: String,
    default: UPLOAD_ACTION_URL
  },
  accept: {
    type: String,
    default: ''
  },
  submitting: {
    type: Boolean,
    default: false
  },
  beforeUpload: {
    type: Function,
    required: true
  },
  onUploadSuccess: {
    type: Function,
    required: true
  },
  onUploadError: {
    type: Function,
    required: true
  },
  onUploadRemove: {
    type: Function,
    required: true
  },
  onPreview: {
    type: Function,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'update:imageList', 'cancel', 'submit'])

const innerVisible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const imageList = computed({
  get: () => props.imageList,
  set: value => emit('update:imageList', value)
})
</script>

<style scoped>
.upload-tip {
  margin-top: 5px;
  color: var(--color-text-placeholder);
  font-size: var(--font-size-xs);
}

.review-uploader :deep(.el-upload--picture-card),
.review-uploader :deep(.el-upload-list__item) {
  width: 96px;
  height: 96px;
}

.review-uploader :deep(.el-upload--picture-card) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
</style>
