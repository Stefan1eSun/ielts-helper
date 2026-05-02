package com.ielts.helper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ielts.helper.common.JwtUtil;
import com.ielts.helper.entity.dto.LoginDTO;
import com.ielts.helper.entity.dto.RegisterDTO;
import com.ielts.helper.entity.dto.ResetPasswordDTO;
import com.ielts.helper.entity.User;
import com.ielts.helper.entity.response.LoginResponse;
import com.ielts.helper.mapper.UserMapper;
import com.ielts.helper.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String CODE_PREFIX = "verification_code:";
    private static final String SEND_TIME_PREFIX = "send_time:";
    private static final long CODE_EXPIRATION = 10;
    private static final long SEND_INTERVAL = 60;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void register(RegisterDTO dto) {
        if (dto.getPhone() == null || dto.getPhone().length() != 11) {
            throw new RuntimeException("手机号格式不正确");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new RuntimeException("密码至少6位");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("手机号已存在");
        }

        if (!validateCode(dto.getPhone(), dto.getCode(), "register")) {
            throw new RuntimeException("验证码错误");
        }

        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
        userMapper.insert(user);

        deleteCode(dto.getPhone(), "register");
    }

    @Override
    public LoginResponse login(LoginDTO dto) {
        if (dto.getPhone() == null || dto.getPhone().length() != 11) {
            throw new RuntimeException("手机号格式不正确");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new RuntimeException("密码至少6位");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = userMapper.selectOne(wrapper);

        if (user == null || !user.getPassword().equals(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()))) {
            throw new RuntimeException("手机号或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getPhone());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getUserId());
        response.setPhone(user.getPhone());
        return response;
    }

    @Override
    public String sendCode(String phone, String type) {
        if (phone == null || phone.length() != 11) {
            throw new RuntimeException("手机号格式不正确");
        }

        String sendTimeKey = SEND_TIME_PREFIX + phone + ":" + type;
        String lastSendTime = redisTemplate.opsForValue().get(sendTimeKey);

        if (lastSendTime != null) {
            throw new RuntimeException("发送过于频繁，请" + SEND_INTERVAL + "秒后再试");
        }

        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        String codeKey = CODE_PREFIX + phone + ":" + type;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRATION, TimeUnit.MINUTES);

        redisTemplate.opsForValue().set(sendTimeKey, "1", SEND_INTERVAL, TimeUnit.SECONDS);

        System.out.println("验证码: " + code + " (手机号: " + phone + ")");
        return code;
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        if (dto.getPhone() == null || dto.getPhone().length() != 11) {
            throw new RuntimeException("手机号格式不正确");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            throw new RuntimeException("密码至少6位");
        }

        if (!validateCode(dto.getPhone(), dto.getCode(), "reset")) {
            throw new RuntimeException("验证码错误");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes()));
        userMapper.updateById(user);

        deleteCode(dto.getPhone(), "reset");
    }

    private boolean validateCode(String phone, String code, String type) {
        String key = CODE_PREFIX + phone + ":" + type;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            return false;
        }

        return storedCode.equals(code);
    }

    private void deleteCode(String phone, String type) {
        String key = CODE_PREFIX + phone + ":" + type;
        redisTemplate.delete(key);
    }
}
