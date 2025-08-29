package com.example.emergencyassistb4b4.domain.attendance.redis;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.RabbitMQ;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RabbitMQRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // === RabbitMQ 상태 ===
    public void saveRabbitMQState(Long teamId, LocalDateTime joinedAt) {
        RabbitMQ rabbitMQ = new RabbitMQ(false, joinedAt);
        redisTemplate.opsForValue().set(RabbitMQRedisKeyUtil.rabbitMQStateKey(teamId), rabbitMQ);
    }

    public void updateRabbitMQState(Long teamId, LocalDateTime joinedAt) {
        RabbitMQ rabbitMQ = new RabbitMQ(true, joinedAt);
        redisTemplate.opsForValue().set(RabbitMQRedisKeyUtil.rabbitMQStateKey(teamId), rabbitMQ);
    }

    public RabbitMQ getRabbitMQState(Long teamId) {
        return (RabbitMQ) redisTemplate.opsForValue().get(RabbitMQRedisKeyUtil.rabbitMQStateKey(teamId));
    }

    public Set<Long> getAllTrackingStates(){
        Set<String> keys= redisTemplate.keys("rabbitmq:state:*");
        if (keys==null) return Set.of();


        return keys.stream()
                .map(k->k.replace("rabbitmq:state:", ""))
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    public void deleteRabbitMQState(Long teamId) {
        redisTemplate.delete(RabbitMQRedisKeyUtil.rabbitMQStateKey(teamId));
    }

    // === 팀 출석 상태 ===
    public void setTeamTrackingState(Long teamId, String value, long ttlSeconds) {
        redisTemplate.opsForValue().set(RabbitMQRedisKeyUtil.teamTrackingStateKey(teamId), value, ttlSeconds, TimeUnit.SECONDS);
    }

    public String getTeamTrackingState(Long teamId) {
        return (String) redisTemplate.opsForValue().get(RabbitMQRedisKeyUtil.teamTrackingStateKey(teamId));
    }

    public void deleteTeamTrackingState(Long teamId) {
        redisTemplate.delete(RabbitMQRedisKeyUtil.teamTrackingStateKey(teamId));
    }

    public boolean existsTeamTrackingState(Long teamId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RabbitMQRedisKeyUtil.teamTrackingStateKey(teamId)));
    }

    // === [3] 자원봉사자 ID → 유저 ID 매핑 ===
    public void cacheUserIdForVolunteer(Long volunteerId, Long userId) {
        redisTemplate.opsForValue().set(RabbitMQRedisKeyUtil.volunteerUserKey(volunteerId), userId.toString());
    }

    public Long getUserIdForVolunteer(Long volunteerId) {
        String value = (String) redisTemplate.opsForValue().get(RabbitMQRedisKeyUtil.volunteerUserKey(volunteerId));
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void deleteUserIdForVolunteer(Long volunteerId) {
        redisTemplate.delete(RabbitMQRedisKeyUtil.volunteerUserKey(volunteerId));
    }


    // === 팀 위치 GEO 저장 및 조회 ===
    public void addTeamGeoLocation(Long teamId, double lat, double lon) {
        String geoKey = RabbitMQRedisKeyUtil.geoKey(teamId);
        redisTemplate.opsForGeo().add(geoKey, new Point(lon, lat), "center");
        redisTemplate.expire(geoKey, Duration.ofMinutes(30));
    }

    public boolean hasGeoKey(Long teamId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RabbitMQRedisKeyUtil.geoKey(teamId)));
    }

    public boolean radiusSearch(Long teamId, double lat, double lon, int radiusMeters) {
        String geoKey = RabbitMQRedisKeyUtil.geoKey(teamId);
        Circle circle = new Circle(new Point(lon, lat), new Distance(radiusMeters, Metrics.NEUTRAL));
        GeoResults<GeoLocation<Object>> results = redisTemplate.opsForGeo().radius(geoKey, circle);
        return results != null && !results.getContent().isEmpty();
    }

    // === 출석 세션 기록 ===
    public void saveAttendanceRecord(Long volunteerId, boolean isPresent) {
        String key = RabbitMQRedisKeyUtil.attendanceSessionKey(volunteerId);
        String value = LocalDateTime.now().format(FORMATTER) + ":" + (isPresent ? "1" : "0");
        redisTemplate.opsForList().rightPush(key, value);
        redisTemplate.expire(key, Duration.ofHours(24)); // TTL 24시간
    }

    public List<Object> getAttendanceRecords(Long volunteerId) {
        return redisTemplate.opsForList().range(RabbitMQRedisKeyUtil.attendanceSessionKey(volunteerId), 0, -1);
    }

    public void deleteAttendanceRecords(Long volunteerId) {
        redisTemplate.delete(RabbitMQRedisKeyUtil.attendanceSessionKey(volunteerId));
    }
}
