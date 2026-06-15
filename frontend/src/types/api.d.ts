export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
}

export interface PageQuery {
  pageNum: number
  pageSize: number
}
