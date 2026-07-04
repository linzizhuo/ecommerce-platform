import request from '@/utils/request'

export function getSeckillSessions() { return request.get('/seckill/sessions') }
export function doSeckill(sessionId: number) { return request.post(`/seckill/${sessionId}`) }
export function getSeckillResult(sessionId: number) { return request.get(`/seckill/result/${sessionId}`) }
