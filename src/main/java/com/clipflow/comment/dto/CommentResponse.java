package com.clipflow.comment.dto;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private Long videoId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;

    public CommentResponse(
            Long id,
            Long videoId,
            Long userId,
            String content,
            LocalDateTime createdAt) {
        this.id = id;
        this.videoId = videoId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getVideoId() {
        return videoId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}