import request from '@/utils/request'

export function getDistributionInfo() { return request.get('/distribution/my') }
export function registerDistributor(parentId?: number) { return request.post('/distribution/register', null, { params: { parentId } }) }
export function withdraw(amount: number) { return request.post('/distribution/withdraw', null, { params: { amount } }) }
