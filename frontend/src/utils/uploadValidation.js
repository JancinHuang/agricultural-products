export const ALLOWED_IMAGE_ACCEPT = 'image/jpeg,image/png,image/gif,image/webp'

const ALLOWED_IMAGE_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'webp']
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']

export const isAllowedImageFile = (file) => {
  const extension = file.name?.split('.').pop()?.toLowerCase()
  const typeAllowed = file.type ? ALLOWED_IMAGE_TYPES.includes(file.type) : true
  const extensionAllowed = extension ? ALLOWED_IMAGE_EXTENSIONS.includes(extension) : false
  return typeAllowed && extensionAllowed
}

export const allowedImageMessage = '仅支持 JPG、PNG、GIF、WEBP 图片'
