package com.ielts.helper.entity.dto;

import lombok.Data;

@Data
public class ChatRequestDTO {
    private Long conversationId;
    private String message;
}