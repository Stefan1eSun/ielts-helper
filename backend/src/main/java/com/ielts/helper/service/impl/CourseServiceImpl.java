package com.ielts.helper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ielts.helper.entity.Course;
import com.ielts.helper.entity.Enrollment;
import com.ielts.helper.entity.response.CourseResponse;
import com.ielts.helper.enums.CourseType;
import com.ielts.helper.mapper.CourseMapper;
import com.ielts.helper.mapper.EnrollmentMapper;
import com.ielts.helper.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CourseServiceImpl implements CourseService {
    
    @Autowired
    private CourseMapper courseMapper;
    
    @Autowired
    private EnrollmentMapper enrollmentMapper;
    
    @Override
    public List<CourseResponse> getAvailableCourses() {
        List<CourseResponse> courses = courseMapper.selectAvailableCourses();
        for (CourseResponse course : courses) {
            course.setTypeCode(course.getTypeCode());
        }
        return courses;
    }
    
    @Override
    public Map<String, Object> getAvailableCoursesWithPagination(int page, int size) {
        int offset = (page - 1) * size;
        List<CourseResponse> courses = courseMapper.selectAvailableCoursesWithPagination(offset, size);
        for (CourseResponse course : courses) {
            course.setTypeCode(course.getTypeCode());
        }
        int total = courseMapper.countAvailableCourses();
        Map<String, Object> result = new HashMap<>();
        result.put("courses", courses);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);
        return result;
    }
    
    @Override
    public Map<String, Object> reserveCourse(Long userId, Long courseId) {
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enrollment::getUserId, userId)
                .eq(Enrollment::getCourseId, courseId)
                .notIn(Enrollment::getStatus, 4, 5); // 排除已完成和已取消的记录
        
        if (enrollmentMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("您已预约过该课程");
        }
        
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getIsOpen() != 1 || course.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("课程不可预约");
        }
        
        String orderId = "ielts_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
                        String.format("%04d", new Random().nextInt(10000));
        
        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setCourseId(courseId);
        enrollment.setStatus(1); // 1-pending_payment
        enrollment.setOrderId(orderId);
        enrollmentMapper.insert(enrollment);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "请扫描二维码完成支付");
        result.put("code_url", "weixin://wxpay/bizpayurl?pr=" + orderId);
        result.put("order_id", orderId);
        return result;
    }
    
    @Override
    public Map<String, Object> getPaymentStatus(String orderId) {
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enrollment::getOrderId, orderId);
        Enrollment enrollment = enrollmentMapper.selectOne(wrapper);
        
        Map<String, Object> result = new HashMap<>();
        
        if (enrollment == null) {
            result.put("status", "failed");
            return result;
        }
        
        Integer status = enrollment.getStatus();
        if (status == 1) { // pending_payment
            // Mock支付成功
            enrollment.setStatus(2); // 2-confirmed
            enrollment.setPaidAt(LocalDateTime.now());
            enrollmentMapper.updateById(enrollment);
            result.put("status", "paid");
            result.put("enrollment_id", enrollment.getEnrollmentId());
        } else if (status == 2) {
            result.put("status", "paid");
            result.put("enrollment_id", enrollment.getEnrollmentId());
        } else {
            result.put("status", "failed");
        }
        return result;
    }
}
