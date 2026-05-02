package com.ielts.helper.entity.response;

import com.ielts.helper.enums.CourseType;
import lombok.Data;

@Data
public class CourseResponse {
    private Long courseId;
    private String title;
    private String type;
    private Integer typeCode;
    private String teacherName;
    private String startTime;
    private String endTime;
    private Double priceCents;
    private String description;

    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
        if (typeCode != null) {
            CourseType courseType = CourseType.fromCode(typeCode);
            this.type = courseType != null ? courseType.getDescription() : null;
        }
    }
}
