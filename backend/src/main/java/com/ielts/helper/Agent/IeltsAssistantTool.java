package com.ielts.helper.Agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ielts.helper.Agent.dto.*;
import com.ielts.helper.entity.Course;
import com.ielts.helper.entity.Enrollment;
import com.ielts.helper.entity.Teacher;
import com.ielts.helper.entity.response.CourseResponse;
import com.ielts.helper.mapper.CourseMapper;
import com.ielts.helper.mapper.EnrollmentMapper;
import com.ielts.helper.mapper.TeacherMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class IeltsAssistantTool {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Tool(name = "listAvailableIeltsCourses", description = "查询所有可预约的雅思课程信息。返回课程ID、标题、类型、教师姓名、上课时间、价格和描述。")
    public List<CourseInfo> listAvailableIeltsCourses() {
        List<CourseResponse> courses = courseMapper.selectAvailableCourses();
        List<CourseInfo> courseInfoList = new ArrayList<>();

        for (CourseResponse course : courses) {
            CourseInfo info = new CourseInfo();
            info.setCourseId(course.getCourseId());
            info.setTitle(course.getTitle());
            info.setType(course.getType());
            info.setTeacherName(course.getTeacherName());
            info.setStartTime(course.getStartTime());
            info.setEndTime(course.getEndTime());
            info.setPriceCents(course.getPriceCents());
            info.setDescription(course.getDescription());
            courseInfoList.add(info);
        }

        return courseInfoList;
    }

    @Tool(name = "getTeacherInfo", description = "根据教师姓名查询该教师的详细信息，包括资质、教学风格和个人简介。")
    public TeacherInfo getTeacherInfo(String teacherName) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getName, teacherName);
        Teacher teacher = teacherMapper.selectOne(wrapper);

        if (teacher == null) {
            return null;
        }

        TeacherInfo info = new TeacherInfo();
        info.setTeacherId(teacher.getTeacherId());
        info.setName(teacher.getName());
        info.setQualification(teacher.getQualification());
        info.setTeachingStyle(teacher.getTeachingStyle());
        info.setBio(teacher.getBio());

        return info;
    }

    @Tool(name = "getUserReservations", description = "查询当前用户的所有预约记录。返回预约ID、课程标题、教师姓名、上课时间、状态和支付金额。")
    public List<ReservationInfo> getUserReservations(Long userId) {
        List<Map<String, Object>> rawReservations = enrollmentMapper.selectUserCourses(userId);
        List<ReservationInfo> reservations = new ArrayList<>();

        for (Map<String, Object> raw : rawReservations) {
            ReservationInfo info = new ReservationInfo();
            info.setEnrollmentId(((Number) raw.get("enrollment_id")).longValue());

            Object courseTitle = raw.get("course_title");
            info.setCourseTitle(courseTitle != null ? courseTitle.toString() : "");

            Object teacherName = raw.get("teacher_name");
            info.setTeacherName(teacherName != null ? teacherName.toString() : "");

            Object startTime = raw.get("start_time");
            if (startTime != null) {
                info.setStartTime(startTime.toString());
            }

            Object endTime = raw.get("end_time");
            if (endTime != null) {
                info.setEndTime(endTime.toString());
            }

            Object statusObj = raw.get("status");
            Integer status = statusObj != null ? ((Number) statusObj).intValue() : 0;
            info.setStatus(getStatusText(status));

            info.setPaidAmount("待支付");
            info.setPaidAt("");

            reservations.add(info);
        }

        return reservations;
    }

    @Tool(name = "reserveIeltsCourse", description = "为当前用户预约指定ID的雅思课程。成功返回预约信息和订单号。")
    public ReservationResult reserveIeltsCourse(Long userId, Long courseId) {
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enrollment::getUserId, userId)
               .eq(Enrollment::getCourseId, courseId)
               .notIn(Enrollment::getStatus, 4, 5);

        if (enrollmentMapper.selectCount(wrapper) > 0) {
            ReservationResult result = new ReservationResult();
            result.setSuccess(false);
            result.setMessage("您已预约过该课程");
            return result;
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            ReservationResult result = new ReservationResult();
            result.setSuccess(false);
            result.setMessage("课程不存在");
            return result;
        }

        if (course.getIsOpen() != 1) {
            ReservationResult result = new ReservationResult();
            result.setSuccess(false);
            result.setMessage("该课程暂不开放预约");
            return result;
        }

        if (course.getStartTime() != null && course.getStartTime().isBefore(java.time.LocalDateTime.now())) {
            ReservationResult result = new ReservationResult();
            result.setSuccess(false);
            result.setMessage("该课程已开始或已结束");
            return result;
        }

        String orderId = "ielts_" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                        String.format("%04d", new java.util.Random().nextInt(10000));

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setCourseId(courseId);
        enrollment.setStatus(1);
        enrollment.setOrderId(orderId);
        enrollmentMapper.insert(enrollment);

        ReservationResult result = new ReservationResult();
        result.setSuccess(true);
        result.setMessage("预约成功，订单号：" + orderId);
        result.setEnrollmentId(enrollment.getEnrollmentId());
        result.setOrderId(orderId);

        return result;
    }

    @Tool(name = "cancelReservation", description = "取消（删除）用户指定的预约。参数enrollmentId是预约记录ID。")
    public OperationResult cancelReservation(Long userId, Long enrollmentId) {
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enrollment::getUserId, userId)
               .eq(Enrollment::getEnrollmentId, enrollmentId);

        Enrollment enrollment = enrollmentMapper.selectOne(wrapper);

        if (enrollment == null) {
            OperationResult result = new OperationResult();
            result.setSuccess(false);
            result.setMessage("预约记录不存在");
            return result;
        }

        if (enrollment.getStatus() == 4 || enrollment.getStatus() == 5) {
            OperationResult result = new OperationResult();
            result.setSuccess(false);
            result.setMessage("该预约已完成或已取消，无法重复取消");
            return result;
        }

        enrollment.setStatus(5);
        enrollmentMapper.updateById(enrollment);

        OperationResult result = new OperationResult();
        result.setSuccess(true);
        result.setMessage("预约已成功取消");
        return result;
    }

    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知状态";
        }
        switch (status) {
            case 1: return "待支付";
            case 2: return "已预约";
            case 3: return "进行中";
            case 4: return "已完成";
            case 5: return "已取消";
            default: return "未知状态";
        }
    }
}