package com.clipflow.message.controller;

import com.clipflow.auth.context.UserContext;
import com.clipflow.common.ApiResponse;
import com.clipflow.common.PageResponse;
import com.clipflow.message.dto.ConversationResponse;
import com.clipflow.message.dto.MessageResponse;
import com.clipflow.message.dto.SendMessageRequest;
import com.clipflow.message.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(
        name = "私信模块",
        description = "发送私信、会话列表和消息历史"
)
@RestController
@RequestMapping("/api")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "发送私信")
    @PostMapping("/users/{receiverId}/messages")
    public ApiResponse<Long> sendMessage(
            @PathVariable Long receiverId,
            @Valid @RequestBody SendMessageRequest request) {

        Long messageId = messageService.sendMessage(
                UserContext.getUserId(),
                receiverId,
                request.getContent()
        );

        return ApiResponse.success(messageId);
    }

    @Operation(summary = "查询当前用户的会话列表")
    @GetMapping("/conversations")
    public ApiResponse<List<ConversationResponse>>
    getConversations() {

        return ApiResponse.success(
                messageService.getUserConversations(
                        UserContext.getUserId()
                )
        );
    }

    @Operation(summary = "分页查询会话消息历史")
    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResponse<PageResponse<MessageResponse>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {

        return ApiResponse.success(
                messageService.getMessages(
                        conversationId,
                        UserContext.getUserId(),
                        page,
                        size
                )
        );
    }
}