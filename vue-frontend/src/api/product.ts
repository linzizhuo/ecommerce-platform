import request from '@/utils/request'

export function getProductList(params: any) {
  return request.get('/product/list', { params })
}

export function getProductDetail(id: number) {
  return request.get(`/product/${id}`)
}

export function getCategories() {
  return request.get('/product/category/list')
}

export function searchProducts(keyword: string) {
  return request.get('/product/list', { params: { keyword } })
}
