package com.example.emergencyassistb4b4.domain.attendance.socket.controller;

import com.example.emergencyassistb4b4.domain.attendance.socket.service.LocationTrackingService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

@Component
@RequiredArgsConstructor
public class LocationTrackingWebSocketHandler implements WebSocketHandler {

    private final LocationTrackingService trackingService;

    @Override
    // 웹소켓 연결 후 초기 처리
    public void afterConnectionEstablished(WebSocketSession session) {}

    @Override
    // 수신된 메시지 처리
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        if (!(message instanceof TextMessage textMessage)) return;
        trackingService.processMessage(textMessage.getPayload());
    }

    @Override
    // 웹소켓 전송 오류 처리
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        closeSessionSilently(session);
    }

    @Override
    // 웹소켓 연결 종료 처리
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {}

    @Override
    // 부분 메시지 지원 여부
    public boolean supportsPartialMessages() {
        return false;
    }

    // 웹소켓 세션 안전하게 종료
    private void closeSessionSilently(WebSocketSession session) {
        try {
            if (session.isOpen()) session.close(CloseStatus.SERVER_ERROR);
        } catch (Exception ignored) {}
    }
}
