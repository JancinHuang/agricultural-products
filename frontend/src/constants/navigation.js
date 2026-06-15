export const USER_NAV_ITEMS = [
  { label: '首页', path: '/home' },
  { label: '全部商品', path: '/shop' },
  { label: '我的订单', path: '/my-order' },
  { label: '我的收藏', path: '/favorite' },
  { label: 'AI助手', path: '/ai-assistant' }
]

export const USER_FOOTER_LINKS = [
  { label: '全部商品', path: '/shop' },
  { label: '我的订单', path: '/my-order' },
  { label: '我的收藏', path: '/favorite' },
  { label: 'AI助手', path: '/ai-assistant' },
  { label: '个人信息', path: '/profile' }
]

export const ADMIN_MENU_GROUPS = [
  {
    key: 'dashboard',
    path: '/admin/dashboard',
    label: '首页概览',
    icon: 'DataAnalysis'
  },
  {
    key: 'admin-menu',
    label: '系统管理',
    icon: 'Setting',
    children: [
      { path: '/admin/user', label: '用户管理', icon: 'User' },
      { path: '/admin/category', label: '分类管理', icon: 'Grid' },
      { path: '/admin/product', label: '商品管理', icon: 'Goods' },
      { path: '/admin/banner', label: '轮播图管理', icon: 'Picture' },
      { path: '/admin/order', label: '订单管理', icon: 'List' }
    ]
  }
]
