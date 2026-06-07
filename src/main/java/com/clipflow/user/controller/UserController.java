package com.clipflow.user.controller;

import com.clipflow.common.ApiResponse;
import com.clipflow.user.dto.LoginRequest;
import com.clipflow.user.dto.RegisterRequest;
import com.clipflow.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<Long> register(
        @Valid @RequestBody RegisterRequest request
    )
    {
        Long userId = userService.register(request);
        return ApiResponse.success(userId);
    }

    @PostMapping("/login")
    public ApiResponse<Long> login(
            @Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }
}
