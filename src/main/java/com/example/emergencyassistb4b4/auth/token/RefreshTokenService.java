package com.example.emergencyassistb4b4.auth.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

// 리프레시 토큰 저장소(Redis)와의 입출력 처리, Refresh 토큰을 저장/조회/삭제 ( RedisTemplate 을 사용 )
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

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(getKey(userId));
    }
}
