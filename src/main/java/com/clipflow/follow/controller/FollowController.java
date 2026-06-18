package com.clipflow.follow.controller;

import com.clipflow.auth.context.UserContext;
import com.clipflow.common.ApiResponse;
import com.clipflow.follow.dto.FollowStatusResponse;
import com.clipflow.follow.service.FollowService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "关注模块", description = "关注、取消关注和查询关注状态")
@RestController
@RequestMapping("/api/users/{followeeId}/follow")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @Operation(summary = "关注用户")
    @PostMapping
    public ApiResponse<Void> follow(
            @PathVariable Long followeeId) {

        followService.follow(
                UserContext.getUserId(),
                followeeId
        );

        return ApiResponse.success(null);
    }

    @Operation(summary = "取消关注用户")
    @DeleteMapping
    public ApiResponse<Void> unfollow(
            @PathVariable Long followeeId) {

        followService.unfollow(
                UserContext.getUserId(),
                followeeId
        );

        return ApiResponse.success(null);
    }

    @Operation(summary = "查询关注状态")
    @GetMapping
    public ApiResponse<FollowStatusResponse> getStatus(
            @PathVariable Long followeeId) {

        boolean following = followService.isFollowing(
                UserContext.getUserId(),
                followeeId
        );

        return ApiResponse.success(
                new FollowStatusResponse(following)
        );
    }
}