package com.clipflow.feed.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clipflow.common.PageResponse;
import com.clipflow.common.exception.BusinessException;
import com.clipflow.follow.service.FollowService;
import com.clipflow.storage.service.StorageService;
import com.clipflow.video.dto.VideoDetailResponse;
import com.clipflow.video.entity.Video;
import com.clipflow.video.mapper.VideoMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private static final String FEED_KEY = "feed:";

    private final FollowService followService;
    private final VideoMapper videoMapper;
    private final StorageService storageService;
    private final StringRedisTemplate redisTemplate;

    public FeedService(
            FollowService followService,
            VideoMapper videoMapper,
            StorageService storageService,
            StringRedisTemplate redisTemplate) {
        this.followService = followService;
        this.videoMapper = videoMapper;
        this.storageService = storageService;
        this.redisTemplate = redisTemplate;
    }

    private String buildFeedKey(Long userId) {
        return FEED_KEY + userId;
    }

    private void rebuildFeed(Long userId) {

        String feedKey = buildFeedKey(userId);
        redisTemplate.delete(feedKey);

        Set<Long> followingIds =
                followService.getFollowingIds(userId);

        if (followingIds.isEmpty()) {
            return;
        }

        List<Video> videos = videoMapper.selectList(
                new LambdaQueryWrapper<Video>()
                        .in(Video::getUserId, followingIds)
                        .eq(Video::getStatus, "PUBLISHED")
                        .orderByDesc(Video::getCreatedAt)
                        .last("LIMIT 200")
        );

        for (Video video : videos) {
            double score = video.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            redisTemplate.opsForZSet().add(
                    feedKey,
                    video.getId().toString(),
                    score
            );
        }
    }

    public PageResponse<VideoDetailResponse> getFeed(
            Long userId,
            long page,
            long size) {

        if (page < 1 || size < 1 || size > 50) {
            throw new BusinessException(25001, "分页参数不合法");
        }

        rebuildFeed(userId);

        String feedKey = buildFeedKey(userId);

        long start = (page - 1) * size;
        long end = start + size - 1;

        Set<String> videoIdStrings =
                redisTemplate.opsForZSet()
                        .reverseRange(feedKey, start, end);

        Long total = redisTemplate.opsForZSet().size(feedKey);

        if (videoIdStrings == null || videoIdStrings.isEmpty()) {
            return new PageResponse<>(
                    total == null ? 0L : total,
                    page,
                    size,
                    List.of()
            );
        }

        List<Long> videoIds = videoIdStrings.stream()
                .map(Long::valueOf)
                .toList();

        List<Video> videos = videoMapper.selectBatchIds(videoIds);

        Map<Long, Video> videoMap = videos.stream()
                .collect(Collectors.toMap(
                        Video::getId,
                        Function.identity()
                ));

        List<VideoDetailResponse> records = new ArrayList<>();

        for (Long videoId : videoIds) {
            Video video = videoMap.get(videoId);

            if (video != null) {
                records.add(toResponse(video));
            }
        }

        return new PageResponse<>(
                total == null ? 0L : total,
                page,
                size,
                records
        );
    }

    private VideoDetailResponse toResponse(Video video) {
        return new VideoDetailResponse(
                video.getId(),
                video.getUserId(),
                video.getTitle(),
                video.getDescription(),
                storageService.getPresignedUrl(
                        video.getObjectName()
                ),
                video.getStatus(),
                video.getCreatedAt()
        );
    }
}