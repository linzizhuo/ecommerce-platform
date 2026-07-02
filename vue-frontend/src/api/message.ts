import request from '@/utils/request'

export function getMessages(isRead?: number) { return request.get('/message/list', { params: { isRead } }) }
export function markRead(id: number) { return request.put(`/message/read/${id}`) }
export function markAllRead() { return request.put('/message/readAll') }
export function unreadCount() { return request.get('/message/unreadCount') }
