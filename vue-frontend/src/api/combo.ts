import request from '@/utils/request'

export function getComboList(merchantId?: number) { return request.get('/combo/list', { params: { merchantId } }) }
export function getComboDetail(id: number) { return request.get(`/combo/${id}`) }
