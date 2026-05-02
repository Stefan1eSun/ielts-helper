package com.ielts.helper.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ielts.helper.Utils.OssUtils;
import com.ielts.helper.entity.dto.UpdateProfileDTO;
import com.ielts.helper.entity.Course;
import com.ielts.helper.entity.Enrollment;
import com.ielts.helper.entity.User;
import com.ielts.helper.mapper.CourseMapper;
import com.ielts.helper.mapper.EnrollmentMapper;
import com.ielts.helper.mapper.UserMapper;
import com.ielts.helper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private EnrollmentMapper enrollmentMapper;
    
    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private OssUtils ossUtils;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
    @Override
    public User getProfile(Long userId) {
        return userMapper.selectById(userId);
    }
    
    @Override
    public void updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getAge() != null) {
            user.setAge(dto.getAge());
        }
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        
        userMapper.updateById(user);
    }
    
    @Override
    public List<Map<String, Object>> getUserCourses(Long userId) {
        List<Map<String, Object>> courses = enrollmentMapper.selectUserCourses(userId);
        
        LocalDateTime now = LocalDateTime.now();
        for (Map<String, Object> course : courses) {
            Integer status = (Integer) course.get("status");
            LocalDateTime startTime = (LocalDateTime) course.get("start_time");
            
            // 只有pending_payment(1)和confirmed(2)状态可以取消，且课程未开始
            boolean canCancel = (status == 1 || status == 2) && 
                              startTime != null && 
                              startTime.isAfter(now.plusHours(2));
            course.put("canCancel", canCancel);
            
            // 重命名字段为camelCase
            course.put("enrollmentId", course.remove("enrollment_id"));
            course.put("courseTitle", course.remove("course_title"));
            course.put("teacherName", course.remove("teacher_name"));
            course.put("startTime", course.remove("start_time"));
        }
        
        return courses;
    }
    
    @Override
    public IPage<Map<String, Object>> getUserCoursesWithPagination(Long userId, int page, int size) {
        Page<Map<String, Object>> pageParam = new Page<>(page, size);
        IPage<Map<String, Object>> result = enrollmentMapper.selectUserCoursesWithPagination(pageParam, userId);
        
        LocalDateTime now = LocalDateTime.now();
        for (Map<String, Object> course : result.getRecords()) {
            Integer status = (Integer) course.get("status");
            LocalDateTime startTime = (LocalDateTime) course.get("start_time");
            
            // 只有pending_payment(1)和confirmed(2)状态可以取消，且课程未开始
            boolean canCancel = (status == 1 || status == 2) && 
                              startTime != null && 
                              startTime.isAfter(now.plusHours(2));
            course.put("canCancel", canCancel);
            
            // 重命名字段为camelCase
            course.put("enrollmentId", course.remove("enrollment_id"));
            course.put("courseTitle", course.remove("course_title"));
            course.put("teacherName", course.remove("teacher_name"));
            course.put("startTime", course.remove("start_time"));
        }
        
        return result;
    }
    
    @Override
    public void cancelCourse(Long userId, Long enrollmentId) {
        Enrollment enrollment = enrollmentMapper.selectById(enrollmentId);
        
        if (enrollment == null || !enrollment.getUserId().equals(userId)) {
            throw new RuntimeException("预约记录不存在");
        }
        
        Integer status = enrollment.getStatus();
        if (status != 1 && status != 2) { // 只有pending_payment和confirmed状态可以取消
            throw new RuntimeException("课程进行中或已结束，无法取消");
        }
        
        Course course = courseMapper.selectById(enrollment.getCourseId());
        if (course == null || course.getStartTime().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new RuntimeException("课程进行中或已结束，无法取消");
        }
        
        // Mock退款处理
        System.out.println("模拟退款处理...");
        
        enrollment.setStatus(5); // 5-cancelled
        enrollmentMapper.updateById(enrollment);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的图片");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("图片大小不能超过5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("只支持JPG、PNG格式的图片");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String fileName = ossUtils.generateFileName(originalFilename);
            String objectKey = "avatars/" + userId + "/" + fileName;

            String url = ossUtils.uploadFile(file.getInputStream(), objectKey, contentType);

            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            user.setAvatarUrl(url);
            userMapper.updateById(user);

            return url;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }
}
