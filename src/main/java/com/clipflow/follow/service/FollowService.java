package com.clipflow.follow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clipflow.common.exception.BusinessException;
import com.clipflow.follow.entity.Follow;
import com.clipflow.follow.mapper.FollowMapper;
import com.clipflow.user.mapper.UserMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FollowService {

    private static final String FOLLOWING_KEY =
            "user:following:";

    private final FollowMapper followMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    public FollowService(
            FollowMapper followMapper,
            UserMapper userMapper,
            StringRedisTemplate redisTemplate) {
        this.followMapper = followMapper;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    private String buildFollowingKey(Long followerId) {
        return FOLLOWING_KEY + followerId;
    }

    public void follow(Long followerId, Long followeeId) {

        if (followerId.equals(followeeId)) {
            throw new BusinessException(24001, "不能关注自己");
        }

        if (userMapper.selectById(followeeId) == null) {
            throw new BusinessException(24002, "用户不存在");
        }

        Long count = followMapper.selectCount(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, followerId)
                        .eq(Follow::getFolloweeId, followeeId)
        );

        if (count == 0) {
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFolloweeId(followeeId);
            follow.setCreatedAt(LocalDateTime.now());

            followMapper.insert(follow);
        }

        redisTemplate.opsForSet().add(
                buildFollowingKey(followerId),
                followeeId.toString()
        );
    }

    public void unfollow(Long followerId, Long followeeId) {

        followMapper.delete(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, followerId)
                        .eq(Follow::getFolloweeId, followeeId)
        );

        redisTemplate.opsForSet().remove(
                buildFollowingKey(followerId),
                followeeId.toString()
        );
    }

    public boolean isFollowing(
            Long followerId,
            Long followeeId) {

        Boolean cached = redisTemplate.opsForSet()
                .isMember(
                        buildFollowingKey(followerId),
                        followeeId.toString()
                );

        if (Boolean.TRUE.equals(cached)) {
            return true;
        }

        Long count = followMapper.selectCount(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, followerId)
                        .eq(Follow::getFolloweeId, followeeId)
        );

        if (count > 0) {
            redisTemplate.opsForSet().add(
                    buildFollowingKey(followerId),
                    followeeId.toString()
            );
            return true;
        }

        return false;
    }

    public Set<Long> getFollowingIds(Long followerId) {

        Set<String> members = redisTemplate.opsForSet()
                .members(buildFollowingKey(followerId));

        if (members != null && !members.isEmpty()) {
            return members.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toSet());
        }

        List<Follow> follows = followMapper.selectList(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, followerId)
        );

        Set<Long> ids = follows.stream()
                .map(Follow::getFolloweeId)
                .collect(Collectors.toSet());

        if (!ids.isEmpty()) {
            redisTemplate.opsForSet().add(
                    buildFollowingKey(followerId),
                    ids.stream()
                            .map(String::valueOf)
                            .toArray(String[]::new)
            );
        }

        return ids;
    }
}