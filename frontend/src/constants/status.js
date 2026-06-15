export const ENABLE_STATUS = {
  enabled: 1,
  disabled: 0
}

export const ENABLE_STATUS_OPTIONS = [
  { label: '启用', value: ENABLE_STATUS.enabled, type: 'success' },
  { label: '禁用', value: ENABLE_STATUS.disabled, type: 'danger' }
]

export const PRODUCT_STATUS_OPTIONS = [
  { label: '上架', value: ENABLE_STATUS.enabled, type: 'success' },
  { label: '下架', value: ENABLE_STATUS.disabled, type: 'danger' }
]

export const ORDER_STATUS_OPTIONS = [
  { label: '待付款', value: 0, type: 'warning' },
  { label: '待发货', value: 1, type: 'info' },
  { label: '待收货', value: 2, type: 'primary' },
  { label: '已完成', value: 3, type: 'success' },
  { label: '已取消', value: 4, type: 'danger' }
]

export const DEFAULT_PAGE_SIZES = [10, 20, 50, 100]

export const IMAGE_LIMITS = {
  icon: 2,
  common: 5
}
