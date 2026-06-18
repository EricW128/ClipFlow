package com.clipflow.message.dto;

import java.time.LocalDateTime;

public class ConversationResponse {

    private Long conversationId;
    private Long otherUserId;
    private String otherUserNickname;
    private String otherUserAvatarUrl;
    private String lastMessageContent;
    private LocalDateTime lastMessageAt;

    public ConversationResponse(
            Long conversationId,
            Long otherUserId,
            String otherUserNickname,
            String otherUserAvatarUrl,
            String lastMessageContent,
            LocalDateTime lastMessageAt) {
        this.conversationId = conversationId;
        this.otherUserId = otherUserId;
        this.otherUserNickname = otherUserNickname;
        this.otherUserAvatarUrl = otherUserAvatarUrl;
        this.lastMessageContent = lastMessageContent;
        this.lastMessageAt = lastMessageAt;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public Long getOtherUserId() {
        return otherUserId;
    }

    public String getOtherUserNickname() {
        return otherUserNickname;
    }

    public String getOtherUserAvatarUrl() {
        return otherUserAvatarUrl;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }
}