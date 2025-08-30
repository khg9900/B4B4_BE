package com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TTLRedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String COUNT_KEY_FORMAT = "team:%d:%d:count";
    private static final String USERS_KEY_FORMAT = "team:%d:%d:users";
    private static final String DUPLICATE_KEY_FORMAT = "team:%d:%d:%d:duplicate";

    /**
     * TTL 설정 - Duration 기반
     */
    public void setTeamKeyTTL(Long postId, Long teamId, Long userId, Duration duration) {
        long seconds = duration.isNegative() ? 1 : duration.getSeconds(); // 최소 1초
        setTeamKeyTTL(postId, teamId, userId, seconds, TimeUnit.SECONDS);
    }

    /**
     * TTL 설정 - 직접 시간 단위
     */
    public void setTeamKeyTTL(Long postId, Long teamId, Long userId, long duration, TimeUnit unit) {
        String countKey = String.format(COUNT_KEY_FORMAT, postId, teamId);
        String usersKey = String.format(USERS_KEY_FORMAT, postId, teamId);

        redisTemplate.expire(countKey, duration, unit);
        redisTemplate.expire(usersKey, duration, unit);

        if (userId != null) {
            String duplicateKey = String.format(DUPLICATE_KEY_FORMAT, postId, teamId, userId);
            redisTemplate.expire(duplicateKey, duration, unit);
        }
    }

    /**
     * 팀 키 삭제
     */
    public void deleteTeamKeys(Long postId, Long teamId, Long userId) {
        String countKey = String.format(COUNT_KEY_FORMAT, postId, teamId);
        String usersKey = String.format(USERS_KEY_FORMAT, postId, teamId);
        redisTemplate.delete(countKey);
        redisTemplate.delete(usersKey);

        if (userId != null) {
            String duplicateKey = String.format(DUPLICATE_KEY_FORMAT, postId, teamId, userId);
            redisTemplate.delete(duplicateKey);
        }
    }
}
