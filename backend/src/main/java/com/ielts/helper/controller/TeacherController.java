package com.ielts.helper.controller;

import com.ielts.helper.common.Result;
import com.ielts.helper.entity.Teacher;
import com.ielts.helper.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TeacherController {
    
    @Autowired
    private TeacherService teacherService;
    
    @GetMapping("/teachers")
    public Result<List<Teacher>> getAllTeachers() {
        List<Teacher> teachers = teacherService.getAllTeachers();
        return Result.success(teachers);
    }
}
