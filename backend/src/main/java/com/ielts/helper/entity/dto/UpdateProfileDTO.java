package com.ielts.helper.entity.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String username;
    private Integer gender;
    private Integer age;
    private String avatarUrl;
}
