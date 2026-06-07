package com.clipflow.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clipflow.common.exception.BusinessException;
import com.clipflow.user.dto.LoginRequest;
import com.clipflow.user.dto.RegisterRequest;
import com.clipflow.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import com.clipflow.user.entity.User;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Long register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );

        if (count > 0) {
            throw new BusinessException(10001, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setNickname(request.getUsername());
        String passwordHash = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
        user.setPasswordHash(passwordHash);
        userMapper.insert(user);
        return user.getId();
    }

    public Long login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new BusinessException(10002, "用户名或密码错误");
        }

        boolean matched = BCrypt.checkpw(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!matched) {
            throw new BusinessException(10002, "用户名或密码错误");
        }

        return user.getId();
    }
}