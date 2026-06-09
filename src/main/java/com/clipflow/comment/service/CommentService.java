package com.clipflow.comment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clipflow.comment.dto.CommentResponse;
import com.clipflow.comment.dto.CreateCommentRequest;
import com.clipflow.comment.entity.Comment;
import com.clipflow.comment.mapper.CommentMapper;
import com.clipflow.common.PageResponse;
import com.clipflow.common.exception.BusinessException;
import com.clipflow.video.mapper.VideoMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final VideoMapper videoMapper;

    public CommentService(
            CommentMapper commentMapper,
            VideoMapper videoMapper) {
        this.commentMapper = commentMapper;
        this.videoMapper = videoMapper;
    }

    public CommentResponse create(
            Long userId,
            Long videoId,
            CreateCommentRequest request) {

        if (videoMapper.selectById(videoId) == null) {
            throw new BusinessException(22001, "视频不存在");
        }

        Comment comment = new Comment();
        comment.setVideoId(videoId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());

        commentMapper.insert(comment);

        return toResponse(comment);
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getVideoId(),
                comment.getUserId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    public PageResponse<CommentResponse> getComments(
            Long videoId,
            long page,
            long size) {

        if (videoMapper.selectById(videoId) == null) {
            throw new BusinessException(22001, "视频不存在");
        }

        if (page < 1 || size < 1 || size > 50) {
            throw new BusinessException(22002, "分页参数不合法");
        }

        Page<Comment> commentPage = commentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getVideoId, videoId)
                        .orderByDesc(Comment::getCreatedAt)
        );

        List<CommentResponse> records =
                commentPage.getRecords()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return new PageResponse<>(
                commentPage.getTotal(),
                page,
                size,
                records
        );
    }

    public void delete(Long userId, Long videoId, Long commentId) {

        Comment comment = commentMapper.selectById(commentId);

        if (comment == null) {
            throw new BusinessException(22003, "评论不存在");
        }

        if (!comment.getVideoId().equals(videoId)) {
            throw new BusinessException(22003, "评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(22004, "无权删除该评论");
        }

        int deleted = commentMapper.deleteById(commentId);

        if (deleted == 0) {
            throw new BusinessException(22005, "评论删除失败");
        }
    }
}