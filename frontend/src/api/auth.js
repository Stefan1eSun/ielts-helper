import request from '@/utils/request';

// 登录接口
export function login(data) {
    return request.post('/api/auth/login', data);
}

// 注册接口
export function register(data) {
    return request.post('/api/auth/register', data);
}

// 发送验证码接口
export function sendCode(phone, type) {
    return request.post('/api/auth/send-code', { phone, type });
}

// 重置密码接口
export function resetPassword(data) {
    return request.post('/api/auth/reset-password', data);
}
