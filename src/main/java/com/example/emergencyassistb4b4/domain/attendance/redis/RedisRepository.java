package com.example.emergencyassistb4b4.domain.attendance.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String TEAM_TRACKING_STATE_PREFIX = "team:attendance:";
    private static final String GEO_KEY_PREFIX = "attendance:geo:team:";
    private static final String PARTICIPANT_TEAM_PREFIX = "volunteer_user:";
    private static final String ATTENDANCE_SESSION_PREFIX = "attendance:session:";
    private static final String RABBITMQ_STATE = "rabbitmq:state:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // === RabbitMQ 상태 ===
    private String getRabbitMQStateKey(Long teamId) {
        return RABBITMQ_STATE + teamId;
    }

    public void setRabbitMQState(Long teamId) {
        redisTemplate.opsForValue().set(getRabbitMQStateKey(teamId), "false");
    }

    public void changeRabbitMQState(Long teamId) {
        redisTemplate.opsForValue().set(getRabbitMQStateKey(teamId), "true");
    }

    public String getRabbitmqState(Long teamId){
        return redisTemplate.opsForValue().get(getRabbitMQStateKey(teamId));
    }


    public void deleteRabbitMQState(Long teamId) {
        redisTemplate.delete(getRabbitMQStateKey(teamId));
    }

    // === 팀 출석 상태 ===
    public void setTeamTrackingState(Long teamId, String value, long ttlSeconds) {
        String key = TEAM_TRACKING_STATE_PREFIX + teamId;
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    public String getTeamTrackingState(Long teamId) {
        String key = TEAM_TRACKING_STATE_PREFIX + teamId;
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteTeamTrackingState(Long teamId) {
        String key = TEAM_TRACKING_STATE_PREFIX + teamId;
        redisTemplate.delete(key);
    }

    public boolean existsTeamTrackingState(Long teamId) {
        String key = TEAM_TRACKING_STATE_PREFIX + teamId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // === 자원봉사자 -> 팀ID 매핑 ===
    public void cacheTeamIdForVolunteer(Long volunteerId, Long teamId, Duration ttl) {
        String key = PARTICIPANT_TEAM_PREFIX + volunteerId;
        redisTemplate.opsForValue().set(key, teamId.toString(), ttl);
    }

    public Long getTeamIdForVolunteer(Long volunteerId) {
        String key = PARTICIPANT_TEAM_PREFIX + volunteerId;
        String value = redisTemplate.opsForValue().get(key);
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void deleteTeamIdForVolunteer(Long volunteerId) {
        String key = PARTICIPANT_TEAM_PREFIX + volunteerId;
        redisTemplate.delete(key);
    }

    // === 팀 위치 GEO 저장 및 조회 ===
    public void addTeamGeoLocation(Long teamId, double lat, double lon) {
        String geoKey = GEO_KEY_PREFIX + teamId;
        redisTemplate.opsForGeo().add(geoKey, new Point(lon, lat), "center");
        redisTemplate.expire(geoKey, Duration.ofMinutes(30));
    }

    public boolean hasGeoKey(Long teamId) {
        String geoKey = GEO_KEY_PREFIX + teamId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(geoKey));
    }

    public boolean radiusSearch(Long teamId, double lat, double lon, int radiusMeters) {
        String geoKey = GEO_KEY_PREFIX + teamId;
        Circle circle = new Circle(new Point(lon, lat), new Distance(radiusMeters, Metrics.NEUTRAL));
        GeoResults<GeoLocation<String>> results = redisTemplate.opsForGeo().radius(geoKey, circle);
        return results != null && !results.getContent().isEmpty();
    }

    // === 출석 세션 기록 ===
    public void saveAttendanceRecord(Long volunteerId, boolean isPresent) {
        String key = ATTENDANCE_SESSION_PREFIX + volunteerId;
        String value = LocalDateTime.now().format(FORMATTER) + ":" + (isPresent ? "1" : "0");
        redisTemplate.opsForList().rightPush(key, value);
        redisTemplate.expire(key, Duration.ofHours(24)); // TTL 추가 (예시)
    }

    public List<String> getAttendanceRecords(Long volunteerId) {
        String key = ATTENDANCE_SESSION_PREFIX + volunteerId;
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public void deleteAttendanceRecords(Long volunteerId) {
        String key = ATTENDANCE_SESSION_PREFIX + volunteerId;
        redisTemplate.delete(key);
    }
}
