package com.ielts.helper.task;

import com.ielts.helper.entity.Course;
import com.ielts.helper.entity.Enrollment;
import com.ielts.helper.mapper.CourseMapper;
import com.ielts.helper.mapper.EnrollmentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CourseStatusTask {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    /**
     * 每1分钟检查一次课程状态
     */
    @Scheduled(cron = "0 * * * * ?")
    public void updateCourseStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        // 更新课程表状态
        updateCoursesStatus(now);
        
        // 更新预约表状态
        updateEnrollmentsStatus(now);
    }

    /**
     * 更新课程表状态
     */
    private void updateCoursesStatus(LocalDateTime now) {
        // 查找已过期但仍开放的课程
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getIsOpen, 1)
                .lt(Course::getEndTime, now)
                .eq(Course::getDeleted, 0);
        
        List<Course> courses = courseMapper.selectList(wrapper);
        for (Course course : courses) {
            course.setIsOpen(0);
            courseMapper.updateById(course);
            System.out.println("课程已关闭: " + course.getTitle());
        }
    }

    /**
     * 更新预约表状态
     */
    private void updateEnrollmentsStatus(LocalDateTime now) {
        // 查找所有未完成的预约
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.notIn(Enrollment::getStatus, 4, 5) // 排除已完成和已取消
                .eq(Enrollment::getDeleted, 0);
        
        List<Enrollment> enrollments = enrollmentMapper.selectList(wrapper);
        for (Enrollment enrollment : enrollments) {
            Course course = courseMapper.selectById(enrollment.getCourseId());
            if (course == null) continue;
            
            LocalDateTime startTime = course.getStartTime();
            LocalDateTime endTime = course.getEndTime();
            LocalDateTime createdAt = enrollment.getCreatedAt();
            
            Integer currentStatus = enrollment.getStatus();
            Integer newStatus = currentStatus;
            
            if (currentStatus == 1) { // pending_payment
                // 未完成支付的预约在10分钟后自动取消
                if (createdAt != null && now.isAfter(createdAt.plusMinutes(10))) {
                    newStatus = 5; // cancelled
                }
            } else if (currentStatus == 2) { // confirmed
                if (now.isAfter(endTime)) {
                    // 课程已结束
                    newStatus = 4; // completed
                } else if (now.isAfter(startTime)) {
                    // 课程进行中
                    newStatus = 3; // in_progress
                }
            } else if (currentStatus == 3) { // in_progress
                if (now.isAfter(endTime)) {
                    // 课程已结束
                    newStatus = 4; // completed
                }
            }
            
            if (!currentStatus.equals(newStatus)) {
                enrollment.setStatus(newStatus);
                enrollmentMapper.updateById(enrollment);
                System.out.println("预约状态更新: " + enrollment.getEnrollmentId() + " -> " + newStatus);
            }
        }
    }
}
