import request from '@/utils/request'

export function suggest(keyword: string) { return request.get('/search/suggest', { params: { keyword } }) }
export function search(keyword: string) { return request.get('/search', { params: { keyword } }) }
export function hotWords() { return request.get('/search/hot') }
