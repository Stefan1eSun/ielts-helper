package com.ielts.helper.service;

import com.ielts.helper.entity.dto.LoginDTO;
import com.ielts.helper.entity.dto.RegisterDTO;
import com.ielts.helper.entity.dto.ResetPasswordDTO;
import com.ielts.helper.entity.response.LoginResponse;

public interface AuthService {
    void register(RegisterDTO dto);
    LoginResponse login(LoginDTO dto);
    String sendCode(String phone, String type);
    void resetPassword(ResetPasswordDTO dto);
}
