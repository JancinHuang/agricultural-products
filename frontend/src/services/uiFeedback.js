import { ElMessage, ElMessageBox } from 'element-plus'

export const notify = {
  success(message, options = {}) {
    ElMessage.success(typeof message === 'string' ? { message, ...options } : message)
  },
  error(message, options = {}) {
    ElMessage.error(typeof message === 'string' ? { message, ...options } : message)
  },
  warning(message, options = {}) {
    ElMessage.warning(typeof message === 'string' ? { message, ...options } : message)
  },
  info(message, options = {}) {
    ElMessage.info(typeof message === 'string' ? { message, ...options } : message)
  }
}

export function confirmAction(message, options = {}) {
  return ElMessageBox.confirm(message, options.title || '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: options.type || 'info',
    ...options
  })
}

export function confirmDanger(message, options = {}) {
  return confirmAction(message, {
    type: 'warning',
    ...options
  })
}
