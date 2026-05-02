package com.ielts.helper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ielts.helper.entity.Teacher;
import com.ielts.helper.mapper.TeacherMapper;
import com.ielts.helper.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherServiceImpl implements TeacherService {
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Override
    public List<Teacher> getAllTeachers() {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Teacher::getCreatedAt);
        return teacherMapper.selectList(wrapper);
    }
}
