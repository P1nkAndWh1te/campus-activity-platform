package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.BusinessException;
import com.campus.dto.LoginRequest;
import com.campus.dto.LoginResponse;
import com.campus.dto.RegisterRequest;
import com.campus.dto.UserVO;
import com.campus.entity.AppUser;
import com.campus.mapper.AppUserMapper;
import com.campus.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final AppUserMapper appUserMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(AppUserMapper appUserMapper, BCryptPasswordEncoder passwordEncoder) {
        this.appUserMapper = appUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserVO register(RegisterRequest req) {
        // 检查手机号是否已注册
        Long count = appUserMapper.selectCount(
                new LambdaQueryWrapper<AppUser>().eq(AppUser::getPhone, req.getPhone())
        );
        if (count > 0) {
            throw new BusinessException("该手机号已注册");
        }

        AppUser user = new AppUser();
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname());
        user.setRole("USER");
        user.setStatus(1);
        appUserMapper.insert(user);

        return UserVO.from(user);
    }

    public LoginResponse login(LoginRequest req) {
        AppUser user = appUserMapper.selectOne(
                new LambdaQueryWrapper<AppUser>().eq(AppUser::getPhone, req.getPhone())
        );
        if (user == null) {
            throw new BusinessException("手机号或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }

        String token = JwtUtil.createToken(user.getId(), user.getRole());
        return new LoginResponse(token, UserVO.from(user));
    }

    public UserVO getCurrentUser(Long userId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserVO.from(user);
    }
}
