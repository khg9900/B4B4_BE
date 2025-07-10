package com.example.emergencyassistb4b4.volunteer.infra.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TTLRedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String COUNT_KEY_FORMAT = "team:%d:count";
    private static final String USERS_KEY_FORMAT = "team:%d:users";

    /**
     * TTL 설정 - Duration
     */
    public void setTeamKeyTTL(Long teamId, Duration duration) {
        setTeamKeyTTL(teamId, duration.getSeconds(), TimeUnit.SECONDS);
    }

    /**
     * TTL 설정 - 직접 시간 단위
     */
    public void setTeamKeyTTL(Long teamId, long duration, TimeUnit unit) {
        String countKey = String.format(COUNT_KEY_FORMAT, teamId);
        String usersKey = String.format(USERS_KEY_FORMAT, teamId);

        redisTemplate.expire(countKey, duration, unit);
        redisTemplate.expire(usersKey, duration, unit);
    }

    /**
     * 팀 키 삭제
     */
    public void deleteTeamKeys(Long teamId) {
        String countKey = String.format(COUNT_KEY_FORMAT, teamId);
        String usersKey = String.format(USERS_KEY_FORMAT, teamId);

        redisTemplate.delete(countKey);
        redisTemplate.delete(usersKey);
    }
}