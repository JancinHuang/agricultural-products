export const ORDER_STATUS_MAP = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '待发货', type: 'info' },
  2: { text: '待收货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'danger' }
}

export const NEXT_STATUS_MAP = {
  0: '付款',
  1: '发货',
  2: '确认收货'
}

export const getStatusText = (status) => ORDER_STATUS_MAP[status]?.text || '未知'
export const getStatusType = (status) => ORDER_STATUS_MAP[status]?.type || 'info'
export const getNextStatusText = (status) => NEXT_STATUS_MAP[status] || ''
