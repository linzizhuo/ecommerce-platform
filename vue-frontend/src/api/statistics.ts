import request from '@/utils/request'

export function getTodayStats() { return request.get('/statistics/today') }
export function getDailyStats(date?: string) { return request.get('/statistics/daily', { params: { date } }) }
export function getWeekStats() { return request.get('/statistics/week') }
