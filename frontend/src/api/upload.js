import request from '@/utils/request'
import { UPLOAD_ENDPOINT } from '@/constants/endpoints'

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(UPLOAD_ENDPOINT, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
