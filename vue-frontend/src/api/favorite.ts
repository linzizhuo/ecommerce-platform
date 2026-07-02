import request from '@/utils/request'

export function addFavorite(productId: number) { return request.post(`/favorite/${productId}`) }
export function removeFavorite(productId: number) { return request.delete(`/favorite/${productId}`) }
export function checkFavorite(productId: number) { return request.get(`/favorite/check/${productId}`) }
export function getFavorites() { return request.get('/favorite/list') }
