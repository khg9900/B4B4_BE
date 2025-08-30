package com.example.emergencyassistb4b4.domain.attendance.socket.handler;

import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import com.example.emergencyassistb4b4.domain.attendance.socket.service.LocationWebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingSocketHandler implements WebSocketHandler {
    private final RabbitMQRedisService rabbitMQRedisService;
    private final LocationWebSocketService locationWebSocketService;
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            closeSession(session, CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        registerSession(userId, session);
    }

    private void registerSession(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) session.close(status);
        } catch (Exception e) {
            log.error("세션 종료 실패", e);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        // 필요 시 구현
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket 전송 오류", exception);
        closeSession(session, CloseStatus.SERVER_ERROR);
        removeSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket 연결 종료: sessionId={}", session.getId());
        removeSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void removeSession(WebSocketSession session) {
        userSessions.values().forEach(sessions -> sessions.remove(session));
        userSessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    // ============= Redis 캐싱 =============
    public void cacheVolunteerUserMapping(Long volunteerId, Long userId) {
        rabbitMQRedisService.mapVolunteerToUser(volunteerId, userId);
    }

    public Long getUserIdByVolunteerId(Long volunteerParticipantId) {
        return rabbitMQRedisService.findUserIdByVolunteer(volunteerParticipantId);
    }

    public void removeVolunteerUserMapping(Long volunteerParticipantId) {
        rabbitMQRedisService.unmapVolunteerFromUser(volunteerParticipantId);
    }

    // ============= WebSocket 메시지 전송 =============
    @Transactional(readOnly = true)
    public void sendToUser(Long volunteerId, String event, Object payload) {
        try {
            Long userId = getUserIdByVolunteerId(volunteerId);
            if (userId == null) {
                log.warn("유저 매핑 없음: volunteerId={}", volunteerId);
                locationWebSocketService.saveAndPublishAttendance(volunteerId, false);
                return;
            }

            WebSocketSession session = null;
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null && !sessions.isEmpty()) {
                session = sessions.iterator().next(); // 단일 세션 선택
            }

            if (session == null || !session.isOpen()) {
                log.warn("웹소켓 세션 없음 또는 닫힘: volunteerId={}, userId={}", volunteerId, userId);
                locationWebSocketService.saveAndPublishAttendance(volunteerId, false);
                removeVolunteerUserMapping(volunteerId);
                return;
            }

            String json = objectMapper.writeValueAsString(Map.of("type", event, "data", payload));
            session.sendMessage(new TextMessage(json));
            log.debug("웹소켓 전송 성공: volunteerId={}, userId={}, event={}", volunteerId, userId, event);

        } catch (Exception e) {
            log.error("웹소켓 전송 실패: volunteerId={}, event={}", volunteerId, event, e);
            locationWebSocketService.saveAndPublishAttendance(volunteerId, false);
            removeVolunteerUserMapping(volunteerId);
        }
    }
}
