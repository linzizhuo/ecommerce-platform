import request from '@/utils/request'

export function applyAfterSale(data: any) { return request.post('/aftersale', data) }
export function getAfterSaleList() { return request.get('/aftersale/list') }
