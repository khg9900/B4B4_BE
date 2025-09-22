package com.example.emergencyassistb4b4.domain.location.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class LocationRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    // 유저 지역 정보 저장 (5분 TTL)
    public void saveRegion(Long userId, String province, String city) {
        String regionKey = LocationRedisKeyUtil.regionKey(province, city);
        redisTemplate.opsForSet().add(regionKey, userId.toString());
        redisTemplate.expire(regionKey, Duration.ofMinutes(5));
    }

    // 특정 지역의 유저 목록 조회
    public Set<Object> getRegionUsers(String regionKey) {
        return redisTemplate.opsForSet().members(regionKey);
    }

    // 좌표 저장 + TTL(1분) 부여
    public void saveCoordinates(Long userId, double latitude, double longitude) {
        String key = LocationRedisKeyUtil.userLocationsKey();
        redisTemplate.opsForGeo().add(key, new Point(longitude, latitude), userId.toString());

        String expireKey = LocationRedisKeyUtil.locationTtlKey(userId);
        redisTemplate.opsForValue().set(expireKey, "1", Duration.ofMinutes(1));
    }
}
