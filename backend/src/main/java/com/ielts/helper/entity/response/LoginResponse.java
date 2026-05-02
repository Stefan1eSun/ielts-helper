package com.ielts.helper.entity.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String phone;
}
