package com.example.emergencyassistb4b4.domain.attendance.redis;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.RabbitMQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RabbitMQRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_PRESENT = "present";


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
        Object obj = redisTemplate.opsForValue().get(RabbitMQRedisKeyUtil.rabbitMQStateKey(teamId));
        return objectMapper.convertValue(obj, RabbitMQ.class);
    }

    public Set<Long> getAllTrackingStates() {
        Set<String> keys = redisTemplate.keys("attendance:rabbitmq:state:*");
        if (keys == null) return Set.of();
        return keys.stream()
                .map(k -> k.replace("attendance:rabbitmq:state:", ""))
                .filter(s -> s.matches("\\d+"))
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    public void deleteRabbitMQState(Long teamId) {
        redisTemplate.delete(RabbitMQRedisKeyUtil.rabbitMQStateKey(teamId));
    }

    // === 팀 출석 상태 ===
    public void setTeamTrackingState(Long teamId, String value, long ttlSeconds) {
        redisTemplate.opsForValue().set(RabbitMQRedisKeyUtil.teamTrackingStateKey(teamId), value, Duration.ofSeconds(ttlSeconds));
    }

    public String getTeamTrackingState(Long teamId) {
        return (String) redisTemplate.opsForValue().get(RabbitMQRedisKeyUtil.teamTrackingStateKey(teamId));
    }

    public void deleteTeamTrackingState(Long teamId) {
        redisTemplate.delete(RabbitMQRedisKeyUtil.teamTrackingStateKey(teamId));
    }

    // === 자원봉사자 ID → 유저 ID 매핑 ===
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

    // === 자원봉사자 → 팀 매핑 ===
    public void mapVolunteerToTeam(Long volunteerId, Long teamId) {
        redisTemplate.opsForValue().set(RabbitMQRedisKeyUtil.volunteerTeamKey(volunteerId), teamId.toString(), Duration.ofMinutes(30));
    }

    public Long findTeamByVolunteer(Long volunteerId) {
        String value = (String) redisTemplate.opsForValue().get(RabbitMQRedisKeyUtil.volunteerTeamKey(volunteerId));
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void unmapVolunteerFromTeam(Long volunteerId) {
        redisTemplate.delete(RabbitMQRedisKeyUtil.volunteerTeamKey(volunteerId));
    }

    // === 팀 위치 GEO 저장 및 조회 ===
    public void addTeamGeoLocation(Long teamId, double lat, double lon, Duration ttl) {
        String geoKey = RabbitMQRedisKeyUtil.geoKey(teamId);
        redisTemplate.opsForGeo().add(geoKey, new Point(lon, lat), "center");
        redisTemplate.expire(geoKey, ttl);
    }

    public boolean hasGeoKey(Long teamId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RabbitMQRedisKeyUtil.geoKey(teamId)));
    }

    public boolean radiusSearch(Long teamId, double lat, double lon, int radiusMeters) {
        String geoKey = RabbitMQRedisKeyUtil.geoKey(teamId);
        Circle circle = new Circle(new Point(lon, lat), new Distance(radiusMeters, Metrics.NEUTRAL));
        return redisTemplate.opsForGeo().radius(geoKey, circle).getContent().stream()
                .anyMatch(r -> "center".equals(r.getContent().getName()));
    }

    // === 출석 세션 기록 ===
    public void saveAttendanceRecord(Long volunteerId, boolean isPresent, Duration ttl) {
        String key = RabbitMQRedisKeyUtil.attendanceSessionKey(volunteerId);
        Map<String, Object> record = Map.of(
                FIELD_TIMESTAMP, LocalDateTime.now().format(FORMATTER),
                FIELD_PRESENT, isPresent
        );
        redisTemplate.opsForList().rightPush(key, record);
        redisTemplate.expire(key, ttl);
    }

    public List<String> fetchAttendanceRecords(Long volunteerId) {
        String key = RabbitMQRedisKeyUtil.attendanceSessionKey(volunteerId);
        List<Object> objects = redisTemplate.opsForList().range(key, 0, -1);
        if (objects == null) return List.of();

        // present 값만 "1"/"0" 문자열로 변환
        return objects.stream()
                .map(obj -> {
                    Map<?, ?> map = objectMapper.convertValue(obj, Map.class);
                    return Boolean.TRUE.equals(map.get(FIELD_PRESENT)) ? "1" : "0";
                })
                .toList();
    }

    public void deleteAttendanceRecords(Long volunteerId) {
        redisTemplate.delete(RabbitMQRedisKeyUtil.attendanceSessionKey(volunteerId));
    }
}
