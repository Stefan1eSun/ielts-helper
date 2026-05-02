package com.ielts.helper.controller;

import com.ielts.helper.common.Result;
import com.ielts.helper.entity.dto.LoginDTO;
import com.ielts.helper.entity.dto.RegisterDTO;
import com.ielts.helper.entity.dto.ResetPasswordDTO;
import com.ielts.helper.entity.dto.SendCodeDTO;
import com.ielts.helper.entity.response.LoginResponse;
import com.ielts.helper.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Result<Void>> register(@RequestBody RegisterDTO dto) {
        try {
            authService.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Result.success("注册成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("已存在")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Result.error(422, e.getMessage()));
            }
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Result<LoginResponse>> login(@RequestBody LoginDTO dto) {
        try {
            LoginResponse data = authService.login(dto);
            return ResponseEntity.ok(Result.success(data));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, e.getMessage()));
        }
    }

    @PostMapping("/send-code")
    public ResponseEntity<Result<Map<String, String>>> sendCode(@RequestBody SendCodeDTO dto) {
        try {
            String type = dto.getType() != null ? dto.getType() : "register";
            String code = authService.sendCode(dto.getPhone(), type);
            Map<String, String> data = new HashMap<>();
            data.put("code", code);
            return ResponseEntity.ok(Result.success(data, "验证码已发送"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Result<Void>> resetPassword(@RequestBody ResetPasswordDTO dto) {
        try {
            authService.resetPassword(dto);
            return ResponseEntity.ok(Result.success("密码重置成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }
}
