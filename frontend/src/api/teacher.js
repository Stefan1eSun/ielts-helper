import request from '@/utils/request';
 

// 获取教师列表接口
export function getTeacherList() {
  return request.get('/api/teachers');
}