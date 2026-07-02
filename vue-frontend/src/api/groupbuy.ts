import request from '@/utils/request'

export function createGroup(activityId: number) { return request.post('/groupbuy/create', { activityId }) }
export function joinGroup(groupId: number) { return request.post(`/groupbuy/join/${groupId}`) }
export function getGroupDetail(groupId: number) { return request.get(`/groupbuy/detail/${groupId}`) }
