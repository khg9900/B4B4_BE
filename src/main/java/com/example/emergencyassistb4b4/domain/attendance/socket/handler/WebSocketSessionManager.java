package com.example.emergencyassistb4b4.domain.attendance.socket.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    // 특정 사용자에게 WebSocket 세션을 추가
    public void addSession(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    // 세션을 제거하고 비어 있는 사용자 매핑도 정리
    public void removeSession(WebSocketSession session) {
        userSessions.values().forEach(sessions -> sessions.remove(session));
        userSessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    // 특정 사용자에게 연결된 세션을 가져옴 (여러 세션 중 하나 반환)
    public WebSocketSession getSession(Long userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) return null;
        WebSocketSession session = sessions.iterator().next();
        return (session != null && session.isOpen()) ? session : null;
    }
}