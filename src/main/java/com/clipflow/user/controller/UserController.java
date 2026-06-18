package com.clipflow.user.controller;

import com.clipflow.auth.context.UserContext;
import com.clipflow.common.ApiResponse;
import com.clipflow.user.dto.LoginRequest;
import com.clipflow.user.dto.RegisterRequest;
import com.clipflow.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@Tag(
        name = "用户模块",
        description = "用户注册、登录和当前用户查询"
)
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户注册")
    @SecurityRequirements
    @PostMapping("/register")
    public ApiResponse<Long> register(
        @Valid @RequestBody RegisterRequest request
    )
    {
        Long userId = userService.register(request);
        return ApiResponse.success(userId);
    }

    @Operation(summary = "用户登录")
    @SecurityRequirements
    @PostMapping("/login")
    public ApiResponse<String> login(
            @Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @Operation(summary = "查询当前用户 ID")
    @GetMapping("/me")
    public ApiResponse<Long> me() {
        Long userId = UserContext.getUserId();
        return ApiResponse.success(userId);
    }
}
