package com.clipflow.video.controller;

import com.clipflow.auth.context.UserContext;
import com.clipflow.common.ApiResponse;
import com.clipflow.common.PageResponse;
import com.clipflow.video.dto.VideoDetailResponse;
import com.clipflow.video.service.VideoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping
    public ApiResponse<Long> publish(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestPart("file") MultipartFile file) {

        Long userId = UserContext.getUserId();

        Long videoId = videoService.publish(
                userId,
                title,
                description,
                file
        );

        return ApiResponse.success(videoId);
    }

    @GetMapping("/{videoId}")
    public ApiResponse<VideoDetailResponse> getDetail(
            @PathVariable Long videoId) {

        return ApiResponse.success(
                videoService.getDetail(videoId)
        );
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<VideoDetailResponse>> getMyVideos(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {

        Long userId = UserContext.getUserId();

        return ApiResponse.success(
                videoService.getMyVideos(userId, page, size)
        );
    }

    @GetMapping("/feed")
    public ApiResponse<PageResponse<VideoDetailResponse>> getFeed(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {

        return ApiResponse.success(
                videoService.getFeed(page, size)
        );
    }

    @DeleteMapping("/{videoId}")
    public ApiResponse<Void> deleteVideo(
            @PathVariable Long videoId) {

        Long userId = UserContext.getUserId();
        videoService.deleteVideo(userId, videoId);

        return ApiResponse.success(null);
    }
}