import request from '@/utils/request'

export function getCart() { return request.get('/cart') }
export function addToCart(data: any) { return request.post('/cart', data) }
export function updateCart(data: any) { return request.put('/cart', data) }
export function removeFromCart(skuId: number) { return request.delete(`/cart/${skuId}`) }
export function checkCartItem(skuId: number) { return request.put(`/cart/check/${skuId}`) }
export function clearCart() { return request.delete('/cart/clear') }
