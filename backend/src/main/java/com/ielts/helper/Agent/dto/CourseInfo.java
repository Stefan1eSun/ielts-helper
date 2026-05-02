package com.ielts.helper.Agent.dto;

import lombok.Data;

@Data
public class CourseInfo {
    private Long courseId;
    private String title;
    private String type;
    private String teacherName;
    private String startTime;
    private String endTime;
    private Double priceCents;
    private String description;
}