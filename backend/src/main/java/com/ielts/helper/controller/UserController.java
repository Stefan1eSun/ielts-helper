package com.ielts.helper.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ielts.helper.common.JwtUtil;
import com.ielts.helper.common.Result;
import com.ielts.helper.entity.dto.UpdateProfileDTO;
import com.ielts.helper.entity.User;
import com.ielts.helper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/profile")
    public Result<User> getProfile(@RequestHeader("Authorization") String authorization) {
        Long userId = getUserIdFromToken(authorization);
        User user = userService.getProfile(userId);
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestHeader("Authorization") String authorization,
                                      @RequestBody UpdateProfileDTO dto) {
        Long userId = getUserIdFromToken(authorization);
        userService.updateProfile(userId, dto);
        return Result.success("更新成功");
    }

    @GetMapping("/courses")
    public Result<IPage<Map<String, Object>>> getUserCourses(@RequestHeader("Authorization") String authorization,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "5") int size) {
        Long userId = getUserIdFromToken(authorization);
        // 限制每页最多5条
        if (size > 5) {
            size = 5;
        }
        IPage<Map<String, Object>> courses = userService.getUserCoursesWithPagination(userId, page, size);
        return Result.success(courses);
    }

    @PostMapping("/courses/{enrollmentId}/cancel")
    public Result<Void> cancelCourse(@RequestHeader("Authorization") String authorization,
                                     @PathVariable Long enrollmentId) {
        Long userId = getUserIdFromToken(authorization);
        try {
            userService.cancelCourse(userId, enrollmentId);
            return Result.success("课程已取消，退款将在1-3个工作日内到账");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    private Long getUserIdFromToken(String authorization) {
        String token = authorization.replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }

    @PostMapping("/avatar/upload")
    public Result<Map<String, String>> uploadAvatar(@RequestHeader("Authorization") String authorization,
                                                    @RequestParam("file") MultipartFile file) {
        try {
            Long userId = getUserIdFromToken(authorization);
            String avatarUrl = userService.uploadAvatar(userId, file);

            Map<String, String> data = new HashMap<>();
            data.put("avatarUrl", avatarUrl);

            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

}
