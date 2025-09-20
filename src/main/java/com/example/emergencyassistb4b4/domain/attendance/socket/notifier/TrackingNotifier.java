package com.example.emergencyassistb4b4.domain.attendance.socket.notifier;

import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import com.example.emergencyassistb4b4.domain.attendance.socket.message.AttendanceStatusMessage;
import com.example.emergencyassistb4b4.domain.attendance.socket.message.TrackingMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final RabbitMQRedisService rabbitMQRedisService;

    // userId → WebSocket 세션 저장
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    //출석 상태를 특정 volunteerId와 연결된 유저에게 전송
    public void notifyTrackingCheck(Long volunteerId, boolean isPresent) {
        Long userId = getUserIdByVolunteerId(volunteerId);
        if (userId == null) return;

        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) return;

        AttendanceStatusMessage statusMessage = new AttendanceStatusMessage(volunteerId, isPresent);
        TrackingMessage<AttendanceStatusMessage> message =
                new TrackingMessage<>("attendance_status", statusMessage);

        try {
            String json = objectMapper.writeValueAsString(message);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception e) {
            log.error("출석 상태 알림 전송 중 오류", e);
        }
    }

    //redis에서 사용자 아이디 조회
    private Long getUserIdByVolunteerId(Long volunteerId) {
        try {
            return rabbitMQRedisService.findUserIdByVolunteer(volunteerId);
        } catch (Exception e) {
            log.error("Redis 조회 중 오류 발생", e);
            return null;
        }
    }
}
