package com.example.emergencyassistb4b4.domain.attendance.socket.handler;

import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

@Component
@RequiredArgsConstructor
public class TrackingSocketHandler implements WebSocketHandler {

    private final RabbitMQRedisService rabbitMQRedisService;
    private final WebSocketSessionManager sessionManager;
    private final WebSocketMessageSender messageSender;

    // 클라이언트와 WebSocket 연결이 성공적으로 맺어졌을 때 호출
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) sessionManager.addSession(userId, session);
        else closeSession(session, CloseStatus.NOT_ACCEPTABLE);
    }

    // 클라이언트가 보낸 메시지를 처리 (현재는 미사용)
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {}

    // 연결 중 에러가 발생했을 때 호출
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        closeSession(session, CloseStatus.SERVER_ERROR);
        sessionManager.removeSession(session);
    }

    // 연결이 정상적으로 종료되었을 때 호출
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.removeSession(session);
    }

    // 부분 메시지를 지원하는지 여부 (현재는 false)
    @Override
    public boolean supportsPartialMessages() { return false; }

    // 세션을 안전하게 종료
    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) session.close(status);
        } catch (Exception ignored) {}
    }

    // 특정 자원봉사자에게 메시지를 전송 (세션 없을 경우 실패 처리)
    public void sendToUser(Long volunteerId, String event, Object payload) {
        Long userId = rabbitMQRedisService.findUserIdByVolunteer(volunteerId);
        WebSocketSession session = (userId != null) ? sessionManager.getSession(userId) : null;

        if (session != null) {
            messageSender.sendMessage(volunteerId, event, payload, session);
        } else {
            messageSender.handleSendFailure(volunteerId);
        }
    }
}
