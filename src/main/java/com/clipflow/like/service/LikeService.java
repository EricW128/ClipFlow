package com.clipflow.like.service;

import com.clipflow.common.exception.BusinessException;
import com.clipflow.video.mapper.VideoMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    private static final String VIDEO_LIKES_KEY = "video:likes:";

    private final StringRedisTemplate redisTemplate;
    private final VideoMapper videoMapper;

    public LikeService(
            StringRedisTemplate redisTemplate,
            VideoMapper videoMapper) {
        this.redisTemplate = redisTemplate;
        this.videoMapper = videoMapper;
    }

    private String buildKey(Long videoId) {
        return VIDEO_LIKES_KEY + videoId;
    }

    private void checkVideoExists(Long videoId) {
        if (videoMapper.selectById(videoId) == null) {
            throw new BusinessException(23001, "视频不存在");
        }
    }

    public void like(Long userId, Long videoId) {
        checkVideoExists(videoId);

        String key = buildKey(videoId);
        String member = userId.toString();

        redisTemplate.opsForSet().add(key, member);
    }

    public void unlike(Long userId, Long videoId) {
        checkVideoExists(videoId);

        String key = buildKey(videoId);
        String member = userId.toString();

        redisTemplate.opsForSet().remove(key, member);
    }

    public boolean isLiked(Long userId, Long videoId) {
        checkVideoExists(videoId);

        Boolean result = redisTemplate.opsForSet()
                .isMember(
                        buildKey(videoId),
                        userId.toString()
                );

        return Boolean.TRUE.equals(result);
    }

    public long getLikeCount(Long videoId) {
        checkVideoExists(videoId);

        Long count = redisTemplate.opsForSet()
                .size(buildKey(videoId));

        return count == null ? 0L : count;
    }

    public void deleteVideoLikes(Long videoId) {
        redisTemplate.delete(buildKey(videoId));
    }
}