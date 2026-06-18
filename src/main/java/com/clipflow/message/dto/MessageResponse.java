package com.clipflow.message.dto;

import java.time.LocalDateTime;

public class MessageResponse {

    private Long messageId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;
    private boolean sentByMe;

    public MessageResponse(
            Long messageId,
            Long senderId,
            String content,
            LocalDateTime createdAt,
            boolean sentByMe) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
        this.sentByMe = sentByMe;
    }

    public Long getMessageId() {
        return messageId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isSentByMe() {
        return sentByMe;
    }
}