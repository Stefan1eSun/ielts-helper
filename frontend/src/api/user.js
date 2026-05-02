import request from '@/utils/request';

// 获取个人信息
export function getProfile() {
    return request.get('/api/user/profile');
}

// 更新个人信息
export function updateProfile(data) {
    return request.put('/api/user/profile', data);
}

// 获取用户课程列表（分页）
export function getUserCourses(page, size) {
    return request.get('/api/user/courses', { params: { page, size } });
}

// 取消课程
export function cancelCourse(enrollmentId) {
    return request.post(`/api/user/courses/${enrollmentId}/cancel`);
}

// 上传头像
export function uploadAvatar(file) {
    const formData = new FormData();
    formData.append('file', file);
    return request.post('/api/user/avatar/upload', formData);
}
