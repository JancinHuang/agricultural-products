import { imageUtils } from '@/utils/imageUtils'

export const withPageDefaults = (pageData) => ({
  list: pageData?.list || [],
  total: Number(pageData?.total || 0)
})

export const toImageUrl = (image, options) => imageUtils.getImageUrl(image, options)

export const normalizeProduct = (product = {}) => ({
  id: product.id ?? null,
  name: product.name || '',
  categoryId: product.categoryId ?? null,
  categoryName: product.categoryName || '',
  description: product.description || '',
  price: Number(product.price || 0),
  stock: Number(product.stock || 0),
  unit: product.unit || '',
  origin: product.origin || '',
  image: product.image || '',
  imageUrl: product.imageUrl || toImageUrl(product.image),
  status: product.status ?? 1,
  sales: Number(product.sales || 0),
  createTime: product.createTime || '',
  updateTime: product.updateTime || ''
})

export const normalizeCategory = (category = {}) => ({
  id: category.id ?? null,
  name: category.name || '',
  description: category.description || '',
  icon: category.icon || '',
  iconUrl: category.iconUrl || toImageUrl(category.icon, { width: 96, height: 96, quality: 80 }),
  sort: Number(category.sort || 0),
  status: category.status ?? 1,
  createTime: category.createTime || '',
  updateTime: category.updateTime || ''
})

export const normalizeBanner = (banner = {}) => ({
  id: banner.id ?? null,
  title: banner.title || '',
  subtitle: banner.subtitle || '',
  buttonText: banner.buttonText || '立即查看',
  linkUrl: banner.linkUrl || '/shop',
  image: banner.image || '',
  imageUrl: banner.imageUrl || toImageUrl(banner.image),
  sort: Number(banner.sort || 0),
  status: banner.status ?? 1
})

export const normalizeOrder = (order = {}) => ({
  ...order,
  totalAmount: Number(order.totalAmount || 0),
  status: order.status ?? 0
})
