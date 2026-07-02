import request from '@/utils/request'

export function getActiveNotices() { return request.get('/notice/active') }
