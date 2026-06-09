package com.clipflow.video.dto;

import java.time.LocalDateTime;

public class VideoDetailResponse {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String videoUrl;
    private String status;
    private LocalDateTime createdAt;

    public VideoDetailResponse(Long id, Long userId, String title, String description,
                               String videoUrl, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
