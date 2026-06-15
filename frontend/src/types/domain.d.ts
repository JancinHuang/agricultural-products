export interface Product {
  id: number
  name: string
  categoryId?: number
  categoryName?: string
  description?: string
  price: number
  stock: number
  unit?: string
  origin?: string
  image?: string
  imageUrl?: string
  status: number
  sales?: number
  createTime?: string
  updateTime?: string
}

export interface Order {
  id: number
  orderNo: string
  userName?: string
  totalAmount: number
  status: number
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  remark?: string
  createTime?: string
}
