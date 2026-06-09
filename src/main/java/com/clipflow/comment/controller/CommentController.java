package com.clipflow.comment.controller;

import com.clipflow.auth.context.UserContext;
import com.clipflow.comment.dto.CommentResponse;
import com.clipflow.comment.dto.CreateCommentRequest;
import com.clipflow.comment.service.CommentService;
import com.clipflow.common.ApiResponse;
import com.clipflow.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos/{videoId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ApiResponse<CommentResponse> create(
            @PathVariable Long videoId,
            @Valid @RequestBody CreateCommentRequest request) {

        Long userId = UserContext.getUserId();

        return ApiResponse.success(
                commentService.create(userId, videoId, request)
        );
    }

    @GetMapping
    public ApiResponse<PageResponse<CommentResponse>> getComments(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {

        return ApiResponse.success(
                commentService.getComments(videoId, page, size)
        );
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> delete(
            @PathVariable Long commentId, @PathVariable Long videoId) {

        Long userId = UserContext.getUserId();
        commentService.delete(userId, videoId, commentId);

        return ApiResponse.success(null);
    }
}