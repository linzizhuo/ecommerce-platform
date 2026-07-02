import request from '@/utils/request'

export function createOrder(data: any) { return request.post('/order', data) }
export function getOrderList(params: any) { return request.get('/order/list', { params }) }
export function getOrderDetail(orderId: number) { return request.get(`/order/${orderId}`) }
export function payOrder(orderId: number, payMethod: number) { return request.post('/order/pay', { orderId, payMethod }) }
export function cancelOrder(orderId: number) { return request.put(`/order/cancel/${orderId}`) }
