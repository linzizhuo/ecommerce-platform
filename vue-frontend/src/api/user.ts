import request from '@/utils/request'

export function login(data: any) { return request.post('/user/login', data) }
export function register(data: any) { return request.post('/user/register', data) }
export function getUserInfo() { return request.get('/user/info') }
export function updateUserInfo(data: any) { return request.put('/user/info', data) }
