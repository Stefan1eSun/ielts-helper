package com.ielts.helper.entity.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    private String phone;
    private String code;
    private String newPassword;
}
