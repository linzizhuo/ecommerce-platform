import request from '@/utils/request'

export function getPresaleList() { return request.get('/presale/list') }
export function payDeposit(presaleId: number) { return request.post('/presale/deposit', { presaleId }) }
export function payFinal(orderId: number) { return request.post('/presale/final', { orderId }) }
