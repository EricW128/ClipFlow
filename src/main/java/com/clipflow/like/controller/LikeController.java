package com.clipflow.like.controller;

import com.clipflow.auth.context.UserContext;
import com.clipflow.common.ApiResponse;
import com.clipflow.like.dto.LikeStatusResponse;
import com.clipflow.like.service.LikeService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "点赞模块", description = "视频点赞、取消点赞和点赞状态查询")
@RestController
@RequestMapping("/api/videos/{videoId}/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @Operation(summary = "点赞视频")
    @PostMapping
    public ApiResponse<Void> like(
            @PathVariable Long videoId) {

        likeService.like(UserContext.getUserId(), videoId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "取消视频点赞")
    @DeleteMapping
    public ApiResponse<Void> unlike(
            @PathVariable Long videoId) {

        likeService.unlike(UserContext.getUserId(), videoId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "查询视频点赞状态和点赞数")
    @GetMapping
    public ApiResponse<LikeStatusResponse> getStatus(
            @PathVariable Long videoId) {

        Long userId = UserContext.getUserId();

        return ApiResponse.success(
                new LikeStatusResponse(
                        likeService.isLiked(userId, videoId),
                        likeService.getLikeCount(videoId)
                )
        );
    }
}