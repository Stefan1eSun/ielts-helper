package com.ielts.helper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ielts.helper.entity.Enrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface EnrollmentMapper extends BaseMapper<Enrollment> {

    @Select("SELECT e.enrollment_id, c.title as course_title, c.type, " +
            "t.name as teacher_name, c.start_time, e.status " +
            "FROM enrollments e " +
            "JOIN courses c ON e.course_id = c.course_id " +
            "JOIN teachers t ON c.teacher_id = t.teacher_id " +
            "WHERE e.user_id = #{userId} AND e.deleted = 0 " +
            "ORDER BY c.start_time DESC")
    List<Map<String, Object>> selectUserCourses(Long userId);

    /**
     * 分页查询用户课程，按课程状态优先级排序
     * 状态优先级：待支付(1) > 已预约(2) > 进行中(3) > 已完成(4) > 已取消(5)
     */
    IPage<Map<String, Object>> selectUserCoursesWithPagination(Page<Map<String, Object>> page, 
                                                                @Param("userId") Long userId);
}