import request from '@/utils/request'

export function getDictTypes() { return request.get('/dict/types') }
export function saveDictType(data: any) { return request.post('/dict/type', data) }
export function updateDictType(id: number, data: any) { return request.put(`/dict/type/${id}`, data) }
export function deleteDictType(id: number) { return request.delete(`/dict/type/${id}`) }
export function getDictItems(typeCode: string) { return request.get(`/dict/type/${typeCode}/items`) }
export function saveDictItem(data: any) { return request.post('/dict/item', data) }
export function updateDictItem(id: number, data: any) { return request.put(`/dict/item/${id}`, data) }
export function deleteDictItem(id: number) { return request.delete(`/dict/item/${id}`) }
