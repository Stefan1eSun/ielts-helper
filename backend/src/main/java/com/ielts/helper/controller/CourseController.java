package com.ielts.helper.controller;

import com.ielts.helper.common.JwtUtil;
import com.ielts.helper.common.Result;
import com.ielts.helper.entity.response.CourseResponse;
import com.ielts.helper.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @GetMapping("/courses")
    public Result<List<CourseResponse>> getAvailableCourses() {
        List<CourseResponse> courses = courseService.getAvailableCourses();
        return Result.success(courses);
    }
    
    @GetMapping("/courses/paginated")
    public Result<Map<String, Object>> getAvailableCoursesWithPagination(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size) {
        Map<String, Object> result = courseService.getAvailableCoursesWithPagination(page, size);
        return Result.success(result);
    }
    
    @PostMapping("/courses/{courseId}/reserve")
    public ResponseEntity<Result<Map<String, Object>>> reserveCourse(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long courseId) {
        try {
            Long userId = getUserIdFromToken(authorization);
            Map<String, Object> result = courseService.reserveCourse(userId, courseId);
            return ResponseEntity.ok(Result.success(result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }
    
    @GetMapping("/payments/orders/{orderId}/status")
    public Result<Map<String, Object>> getPaymentStatus(@PathVariable String orderId) {
        Map<String, Object> status = courseService.getPaymentStatus(orderId);
        return Result.success(status);
    }
    
    private Long getUserIdFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }
        String token = authorization.replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }
}
