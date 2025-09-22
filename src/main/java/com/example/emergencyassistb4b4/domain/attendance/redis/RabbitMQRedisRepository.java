package com.example.emergencyassistb4b4.domain.attendance.redis;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.RabbitMQ;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.geo.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

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

    // ---------------- RabbitMQ 상태 ----------------

    // 팀의 RabbitMQ 상태 저장
    public void saveRabbitMQState(Long teamId, LocalDateTime joinedAt) {
        RabbitMQ rabbitMQ = new RabbitMQ(false, joinedAt);
        redisTemplate.opsForValue().set(RabbitMQRedisKeyUtil.rabbitMQStateKey(teamId), rabbitMQ);
    }

    // 팀의 RabbitMQ 상태 업데이트
    public void updateRabbitMQState(Long teamId, LocalDateTime joinedAt) {
        RabbitMQ rabbitMQ = new RabbitMQ(true, joinedAt);
        redisTemplate.opsForValue().set(RabbitMQRedisKeyUtil.rabbitMQStateKey(teamId), rabbitMQ);
    }

    // 팀의 RabbitMQ 상태 조회
    public RabbitMQ getRabbitMQState(Long teamId) {
        Object obj = redisTemplate.opsForValue().get(RabbitMQRedisKeyUtil.rabbitMQStateKey(teamId));
        return objectMapper.convertValue(obj, RabbitMQ.class);
    }

    // 모든 팀의 트래킹 상태 조회
    public Set<Long> getAllTrackingStates() {
        Set<String> keys = redisTemplate.keys("attendance:rabbitmq:state:*");
        if (keys == null) return Set.of();
        return keys.stream()
                .map(k -> k.replace("attendance:rabbitmq:state:", ""))
                .filter(s -> s.matches("\\d+"))
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    // 팀의 RabbitMQ 상태 삭제
    public void deleteRabbitMQState(Long teamId) {
        redisTemplate.delete(RabbitMQRedisKeyUtil.rabbitMQStateKey(teamId));
    }

    // ---------------- 자원봉사자 - 유저 매핑 ----------------

    // 자원봉사자 ID에 대한 유저 ID 저장
    public void cacheUserIdForVolunteer(Long volunteerId, Long userId) {
        redisTemplate.opsForValue().set(RabbitMQRedisKeyUtil.volunteerUserKey(volunteerId), userId.toString());
    }

    // 자원봉사자 ID로 유저 ID 조회
    public Long getUserIdForVolunteer(Long volunteerId) {
        String value = (String) redisTemplate.opsForValue().get(RabbitMQRedisKeyUtil.volunteerUserKey(volunteerId));
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 자원봉사자 ID와 유저 매핑 삭제
    public void deleteUserIdForVolunteer(Long volunteerId) {
        redisTemplate.delete(RabbitMQRedisKeyUtil.volunteerUserKey(volunteerId));
    }

    // ---------------- 자원봉사자 - 팀 매핑 ----------------

    // 자원봉사자 ID에 팀 ID 매핑
    public void mapVolunteerToTeam(Long volunteerId, Long teamId) {
        redisTemplate.opsForValue().set(RabbitMQRedisKeyUtil.volunteerTeamKey(volunteerId), teamId.toString(), Duration.ofMinutes(30));
    }

    // 자원봉사자 ID로 팀 조회
    public Long findTeamByVolunteer(Long volunteerId) {
        String value = (String) redisTemplate.opsForValue().get(RabbitMQRedisKeyUtil.volunteerTeamKey(volunteerId));
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ---------------- 팀 위치 GEO ----------------

    // 팀의 위치 정보 저장
    public void addTeamGeoLocation(Long teamId, double lat, double lon, Duration ttl) {
        String geoKey = RabbitMQRedisKeyUtil.geoKey(teamId);
        redisTemplate.opsForGeo().add(geoKey, new Point(lon, lat), "center");
        redisTemplate.expire(geoKey, ttl);
    }

    // 팀 위치 정보 존재 여부 확인
    public boolean hasGeoKey(Long teamId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RabbitMQRedisKeyUtil.geoKey(teamId)));
    }

    // 특정 위치 반경 내 팀 존재 여부 확인
    public boolean radiusSearch(Long teamId, double lat, double lon, int radiusMeters) {
        String geoKey = RabbitMQRedisKeyUtil.geoKey(teamId);
        Circle circle = new Circle(new Point(lon, lat), new Distance(radiusMeters, Metrics.NEUTRAL));
        return redisTemplate.opsForGeo().radius(geoKey, circle).getContent().stream()
                .anyMatch(r -> "center".equals(r.getContent().getName()));
    }

    // ---------------- 출석 세션 ----------------

    // 자원봉사자 출석 기록 저장
    public void saveAttendanceRecord(Long volunteerId, boolean isPresent, Duration ttl) {
        String key = RabbitMQRedisKeyUtil.attendanceSessionKey(volunteerId);
        Map<String, Object> record = Map.of(
                FIELD_TIMESTAMP, LocalDateTime.now().format(FORMATTER),
                FIELD_PRESENT, isPresent
        );
        redisTemplate.opsForList().rightPush(key, record);
        redisTemplate.expire(key, ttl);
    }

    // 자원봉사자 출석 기록 조회
    public List<String> fetchAttendanceRecords(Long volunteerId) {
        String key = RabbitMQRedisKeyUtil.attendanceSessionKey(volunteerId);
        List<Object> objects = redisTemplate.opsForList().range(key, 0, -1);
        if (objects == null) return List.of();

        return objects.stream()
                .map(obj -> {
                    Map<?, ?> map = objectMapper.convertValue(obj, Map.class);
                    return Boolean.TRUE.equals(map.get(FIELD_PRESENT)) ? "1" : "0";
                })
                .toList();
    }

    // 자원봉사자 출석 기록 삭제
    public void deleteAttendanceRecords(Long volunteerId) {
        redisTemplate.delete(RabbitMQRedisKeyUtil.attendanceSessionKey(volunteerId));
    }
}
