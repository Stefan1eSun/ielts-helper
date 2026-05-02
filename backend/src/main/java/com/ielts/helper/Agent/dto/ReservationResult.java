package com.ielts.helper.Agent.dto;

import lombok.Data;

@Data
public class ReservationResult {
    private boolean success;
    private String message;
    private Long enrollmentId;
    private String orderId;
}