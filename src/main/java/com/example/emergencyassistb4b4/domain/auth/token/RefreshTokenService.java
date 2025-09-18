package com.example.emergencyassistb4b4.domain.auth.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final long REFRESH_TOKEN_EXPIRE = 60 * 60 * 24 * 7;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public void saveToken(Long userId, String refreshToken) {

        redisTemplate.opsForValue().set(
                getKey(userId),
                refreshToken,
                Duration.ofSeconds(REFRESH_TOKEN_EXPIRE));
    }

    private String getKey(Long userId) {

        return REFRESH_TOKEN_PREFIX + userId;
    }

    public String getRefreshToken(Long userId) {

        return (String) redisTemplate.opsForValue().get(getKey(userId));
    }
}
