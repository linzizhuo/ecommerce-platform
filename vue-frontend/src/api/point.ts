import request from '@/utils/request'

export function getMyPoints() { return request.get('/point/my') }
export function getPointLogs() { return request.get('/point/logs') }
export function getLevels() { return request.get('/point/levels') }
export function exchangeCoupon(pointCost: number) { return request.post('/point/exchange', { pointCost }) }
