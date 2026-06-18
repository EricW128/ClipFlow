package com.clipflow.video.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clipflow.common.PageResponse;
import com.clipflow.common.exception.BusinessException;
import com.clipflow.mq.message.VideoDeleteMessage;
import com.clipflow.mq.producer.VideoDeleteProducer;
import com.clipflow.storage.service.StorageService;
import com.clipflow.video.dto.VideoDetailResponse;
import com.clipflow.video.entity.Video;
import com.clipflow.video.mapper.VideoMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class VideoService {

    private final StorageService storageService;
    private final VideoMapper videoMapper;
    private final VideoDeleteProducer videoDeleteProducer;

    public VideoService(
            StorageService storageService,
            VideoMapper videoMapper,
            VideoDeleteProducer videoDeleteProducer) {
        this.storageService = storageService;
        this.videoMapper = videoMapper;
        this.videoDeleteProducer = videoDeleteProducer;
    }

    public Long publish(
            Long userId,
            String title,
            String description,
            MultipartFile file) {

        if (title == null || title.isBlank()) {
            throw new BusinessException(21001, "视频标题不能为空");
        }

        if (title.length() > 100) {
            throw new BusinessException(21002, "视频标题不能超过100个字符");
        }

        if (description != null && description.length() > 500) {
            throw new BusinessException(21003, "视频描述不能超过500个字符");
        }

        String objectName = storageService.uploadVideo(file, userId);

        try {
            Video video = new Video();
            video.setUserId(userId);
            video.setTitle(title);
            video.setDescription(description);
            video.setObjectName(objectName);
            video.setStatus("PUBLISHED");

            videoMapper.insert(video);

            return video.getId();
        } catch (Exception exception) {
            storageService.delete(objectName);
            throw new BusinessException(21004, "视频投稿失败");
        }
    }

    public VideoDetailResponse getDetail(Long videoId) {

        Video video = videoMapper.selectById(videoId);

        if (video == null) {
            throw new BusinessException(21005, "视频不存在");
        }

        return toDetailResponse(video);
    }

    public PageResponse<VideoDetailResponse> getMyVideos(
            Long userId,
            long page,
            long size) {

        if (page < 1 || size < 1 || size > 50) {
            throw new BusinessException(21006, "分页参数不合法");
        }

        Page<Video> videoPage = videoMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Video>()
                        .eq(Video::getUserId, userId)
                        .orderByDesc(Video::getCreatedAt)
        );

        List<VideoDetailResponse> records =
                videoPage.getRecords()
                        .stream()
                        .map(this::toDetailResponse)
                        .toList();

        return new PageResponse<>(
                videoPage.getTotal(),
                page,
                size,
                records
        );
    }

    private VideoDetailResponse toDetailResponse(Video video) {
        String videoUrl =
                storageService.getPresignedUrl(video.getObjectName());

        return new VideoDetailResponse(
                video.getId(),
                video.getUserId(),
                video.getTitle(),
                video.getDescription(),
                videoUrl,
                video.getStatus(),
                video.getCreatedAt()
        );
    }

    public PageResponse<VideoDetailResponse> getFeed(
            long page,
            long size) {

        if (page < 1 || size < 1 || size > 50) {
            throw new BusinessException(21006, "分页参数不合法");
        }

        Page<Video> videoPage = videoMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Video>()
                        .eq(Video::getStatus, "PUBLISHED")
                        .orderByDesc(Video::getCreatedAt)
        );

        List<VideoDetailResponse> records =
                videoPage.getRecords()
                        .stream()
                        .map(this::toDetailResponse)
                        .toList();

        return new PageResponse<>(
                videoPage.getTotal(),
                page,
                size,
                records
        );
    }

    public void deleteVideo(Long userId, Long videoId) {

        Video video = videoMapper.selectById(videoId);

        if (video == null) {
            throw new BusinessException(
                    21005,
                    "视频不存在"
            );
        }

        if (!video.getUserId().equals(userId)) {
            throw new BusinessException(
                    21007,
                    "无权删除该视频"
            );
        }

        int deleted = videoMapper.deleteById(videoId);

        if (deleted == 0) {
            throw new BusinessException(
                    21008,
                    "视频记录删除失败"
            );
        }

        VideoDeleteMessage message =
                new VideoDeleteMessage(
                        videoId,
                        video.getObjectName()
                );

        videoDeleteProducer.send(message);
    }
}