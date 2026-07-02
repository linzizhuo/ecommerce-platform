import request from '@/utils/request'

export function queryLogistics(orderId: number) { return request.get(`/logistics/${orderId}`) }
export function confirmReceive(orderId: number) { return request.post(`/logistics/sign/${orderId}`) }
