package com.clipflow.feed.controller;

import com.clipflow.auth.context.UserContext;
import com.clipflow.common.ApiResponse;
import com.clipflow.common.PageResponse;
import com.clipflow.feed.service.FeedService;
import com.clipflow.video.dto.VideoDetailResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "信息流模块", description = "查询当前用户关注对象的视频信息流")
@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @Operation(summary = "分页查询关注信息流")
    @GetMapping("/following")
    public ApiResponse<PageResponse<VideoDetailResponse>>
    getFollowingFeed(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {

        Long userId = UserContext.getUserId();

        return ApiResponse.success(
                feedService.getFeed(userId, page, size)
        );
    }
}