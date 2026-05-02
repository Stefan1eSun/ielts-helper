package com.ielts.helper.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ielts.helper.entity.dto.UpdateProfileDTO;
import com.ielts.helper.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {
    User getProfile(Long userId);
    void updateProfile(Long userId, UpdateProfileDTO dto);
    List<Map<String, Object>> getUserCourses(Long userId);
    IPage<Map<String, Object>> getUserCoursesWithPagination(Long userId, int page, int size);
    void cancelCourse(Long userId, Long enrollmentId);
    String uploadAvatar(Long userId, MultipartFile file);
}