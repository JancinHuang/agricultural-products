import { ref } from 'vue'
import { uploadFile } from '@/api/upload'
import { allowedImageMessage, isAllowedImageFile } from '@/utils/uploadValidation'
import { imageUtils } from '@/utils/imageUtils'
import { notify } from '@/services/uiFeedback'

export function useImageUpload(options = {}) {
  const { maxSizeMb = 5, sizeMessage = `图片不能超过 ${maxSizeMb}MB` } = options
  const previewUrl = ref('')
  let localPreviewUrl = ''

  const revokeLocalPreview = () => {
    if (localPreviewUrl) {
      URL.revokeObjectURL(localPreviewUrl)
      localPreviewUrl = ''
    }
  }

  const beforeUpload = (file) => {
    if (!isAllowedImageFile(file)) {
      notify.error(allowedImageMessage)
      return false
    }
    if (file.size / 1024 / 1024 > maxSizeMb) {
      notify.error(sizeMessage)
      return false
    }
    return true
  }

  const upload = async (file) => {
    revokeLocalPreview()
    localPreviewUrl = URL.createObjectURL(file)
    previewUrl.value = localPreviewUrl
    const res = await uploadFile(file)
    const data = res.data || {}
    const objectKey = data.objectKey || data.url || ''
    if (data.url) {
      revokeLocalPreview()
      previewUrl.value = data.url
    } else {
      previewUrl.value = imageUtils.getImageUrl(objectKey)
    }
    return {
      objectKey,
      url: data.url || previewUrl.value,
      filename: data.filename || ''
    }
  }

  const clearPreview = () => {
    revokeLocalPreview()
    previewUrl.value = ''
  }

  return {
    previewUrl,
    beforeUpload,
    upload,
    clearPreview,
    revokeLocalPreview
  }
}
