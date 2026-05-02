package com.ielts.helper.service;

import com.ielts.helper.entity.response.CourseResponse;
import java.util.List;
import java.util.Map;

public interface CourseService {
    List<CourseResponse> getAvailableCourses();
    Map<String, Object> getAvailableCoursesWithPagination(int page, int size);
    Map<String, Object> reserveCourse(Long userId, Long courseId);
    Map<String, Object> getPaymentStatus(String orderId);
}
