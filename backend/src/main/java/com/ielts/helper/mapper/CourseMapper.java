package com.ielts.helper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ielts.helper.entity.Course;
import com.ielts.helper.entity.response.CourseResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    List<CourseResponse> selectAvailableCourses();
    List<CourseResponse> selectAvailableCoursesWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    int countAvailableCourses();
}
