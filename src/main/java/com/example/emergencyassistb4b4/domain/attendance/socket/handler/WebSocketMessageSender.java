package com.example.emergencyassistb4b4.domain.attendance.socket.handler;

import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import com.example.emergencyassistb4b4.domain.attendance.socket.service.LocationWebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketMessageSender {
    private final ObjectMapper objectMapper;
    private final LocationWebSocketService locationWebSocketService;
    private final RabbitMQRedisService rabbitMQRedisService;

    // 지정된 WebSocket 세션에 이벤트 타입과 데이터를 JSON 메시지로 변환해 전송
    public void sendMessage(Long volunteerId, String event, Object payload, WebSocketSession session) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("type", event, "data", payload));
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            handleSendFailure(volunteerId);
        }
    }

    // 메시지 전송 실패 시 출석 상태를 미참석(false)으로 저장하고
    // 해당 봉사자를 사용자 매핑에서 제거
    public void handleSendFailure(Long volunteerId) {
        locationWebSocketService.saveAndPublishAttendance(volunteerId, false);
        rabbitMQRedisService.unmapVolunteerFromUser(volunteerId);
    }
}
