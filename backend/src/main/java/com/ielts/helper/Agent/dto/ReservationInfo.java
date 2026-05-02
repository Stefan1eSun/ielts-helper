package com.ielts.helper.Agent.dto;

import lombok.Data;

@Data
public class ReservationInfo {
    private Long enrollmentId;
    private String courseTitle;
    private String teacherName;
    private String startTime;
    private String endTime;
    private String status;
    private String paidAmount;
    private String paidAt;
}