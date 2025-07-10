package com.example.emergencyassistb4b4.location.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class LocationRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    // 기기 토큰 변경예정
    public void saveRegion(Long userId, String province , String city) {
        String regionKey = "region:" + province + ":" + city;
        redisTemplate.opsForSet().add(regionKey, userId.toString());
        redisTemplate.expire(regionKey, Duration.ofMinutes(5));
    }

    public Set<Object> getRegionUsers(String regionKey) {
        return redisTemplate.opsForSet().members(regionKey);
    }

    public void saveCoordinates(Long userId, double latitude, double longitude) {
        String key = "user:locations";
        redisTemplate.opsForGeo().add(key, new Point(longitude, latitude), userId.toString());

        String expireKey = "location:ttl:" + userId;
        redisTemplate.opsForValue().set(expireKey, "1", Duration.ofMinutes(1));
    }

    public Optional<Point> getCoordinates(String userId) {
        String key = "user:locations";
        List<Point> positions = redisTemplate.opsForGeo().position(key, userId);
        if (positions == null || positions.isEmpty() || positions.get(0) == null) {
            return Optional.empty();
        }
        return Optional.of(positions.get(0));
    }

    public List<Object> findUsersWithinRadius(double latitude, double longitude, int radiusMeters) {
        String key = "user:locations"; // Redis GEO key

        // Redis는 (longitude, latitude) 순서임
        Point center = new Point(longitude, latitude);
        Distance radius = new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS); // m → km
        Circle circle = new Circle(center, radius);

        GeoResults<RedisGeoCommands.GeoLocation<Object>> results =
                redisTemplate.opsForGeo().radius(key, circle);

        if (results == null) return Collections.emptyList();

        return results.getContent().stream()
                .map(r -> r.getContent().getName())
                .collect(Collectors.toList());
    }
}
