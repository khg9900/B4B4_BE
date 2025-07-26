package com.example.emergencyassistb4b4.domain.attendance.socket.handler;

import com.example.emergencyassistb4b4.domain.attendance.redis.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisService redisService;
    // userId → sessions (1:N)
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId"); // attributes에서 userId 꺼내기

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

    // ============= Redis 캐싱 (participantId → userId) =============

    public void cacheVolunteerUserMapping(Long volunteerParticipantId, Long userId) {

        redisService.cacheTeamIdForVolunteer(volunteerParticipantId, userId);
    }

    public Long getUserIdByVolunteerId(Long volunteerParticipantId) {

        String userIdStr = redisService.getTeamIdForVolunteer(volunteerParticipantId).toString();
        if (userIdStr == null) return null;
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void removeVolunteerUserMapping(Long volunteerParticipantId) {
        redisService.deleteTeamIdForVolunteer(volunteerParticipantId);

    }

    // ============= WebSocket 메시지 전송 =============


    @Transactional(readOnly = true)
    public void sendToUser(Long volunteerParticipantId, String event, Object payload) {
        if (volunteerParticipantId == null) {
            log.warn("보낼 대상 volunteerParticipantId가 null입니다.");
            return;
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(Map.of("type", event, "data", payload));
        } catch (Exception e) {
            log.error("메시지 직렬화 실패", e);
            return;
        }

        Long userId = getUserIdByVolunteerId(volunteerParticipantId);
        if (userId == null) {
            log.warn("Redis에서 userId 매핑을 찾을 수 없음: volunteerParticipantId={}", volunteerParticipantId);
            return;
        }

        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            log.warn("WebSocket 세션 없음: userId={}", userId);
            return;
        }

        sessions.removeIf(session -> !session.isOpen());

        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                log.error("WebSocket 메시지 전송 실패: userId={}, volunteerParticipantId={}", userId, volunteerParticipantId, e);
            }
        }
    }
}