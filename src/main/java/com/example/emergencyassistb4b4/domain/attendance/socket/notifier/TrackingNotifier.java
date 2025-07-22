package com.example.emergencyassistb4b4.domain.attendance.socket.notifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrackingNotifier {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StringRedisTemplate redisTemplate; // Redis 접근용
    private static final String VOLUNTEER_USER_PREFIX = "volunteer_user:";

    // userId → WebSocket 세션 저장 (예: TrackingSocketHandler 등에서 관리)
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void notifyTrackingCheck(Long volunteerId, boolean isPresent) {
        Long userId = mapVolunteerToUser(volunteerId);
        if (userId == null) {
            log.warn("volunteerId={}에 매핑된 userId가 없습니다.", volunteerId);
            return;
        }

        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) return;

        Map<String, Object> payload = Map.of(
                "type", "attendance_status",
                "data", Map.of("volunteerId", volunteerId, "isPresent", isPresent)
        );

        try {
            String json = objectMapper.writeValueAsString(payload);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception e) {
            log.error("출석 상태 알림 전송 중 오류", e);
        }
    }

    private Long mapVolunteerToUser(Long volunteerId) {
        try {
            String key = VOLUNTEER_USER_PREFIX + volunteerId;
            String userIdStr = redisTemplate.opsForValue().get(key);
            if (userIdStr == null) {
                log.warn("Redis에서 volunteerId={}에 대한 userId가 없음", volunteerId);
                return null;
            }
            return Long.valueOf(userIdStr);
        } catch (Exception e) {
            log.error("Redis 조회 중 오류 발생", e);
            return null;
        }
    }
}
