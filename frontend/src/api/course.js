import request from '@/utils/request';

// 获取课程列表接口
export function getCourseList() {
    return request.get('/api/courses');
}

// 预约课程接口
export function reserveCourse(courseId) {
    return request.post(`/api/courses/${courseId}/reserve`);
}

// 查询支付状态接口
export function getPaymentStatus(orderId) {
    return request.get(`/api/payments/orders/${orderId}/status`);
}
