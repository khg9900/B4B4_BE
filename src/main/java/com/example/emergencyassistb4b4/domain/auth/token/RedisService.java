package com.example.emergencyassistb4b4.domain.auth.token;

import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    // RefreshToken 삭제
    public void deleteRefreshToken(String email) {

        stringRedisTemplate.delete(REFRESH_TOKEN_PREFIX + email);
    }

    // 블랙리스트에 토큰 추가 (TTL을 초 단위로 변환하여 설정)
    public void addToBlackList(String token, long ttlMillis) {

        if (ttlMillis <= 0) {
            throw new ApiException(ErrorStatus.INVALID_TTL);
        }
        // ttl을 초 단위로 반환
        long ttlSeconds = ttlMillis / 1000;

        // ttl이 초 단위로 제대로´ 설정되도록 설정
        stringRedisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "logout", ttlSeconds, TimeUnit.SECONDS );
    }
}
