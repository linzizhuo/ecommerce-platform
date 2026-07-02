import request from '@/utils/request'

export function sendRedEnvelope(data: any) { return request.post('/red-envelope/send', data) }
export function receiveRedEnvelope(id: number) { return request.post(`/red-envelope/receive/${id}`) }
export function getSentEnvelopes() { return request.get('/red-envelope/sent') }
export function getReceivedEnvelopes() { return request.get('/red-envelope/received') }
