package com.example.emergencyassistb4b4.attendance.socket.controller;

import com.example.emergencyassistb4b4.attendance.socket.dto.LocationUpdateMessage;
import com.example.emergencyassistb4b4.attendance.socket.dto.WebSocketMessageWrapper;
import com.example.emergencyassistb4b4.attendance.socket.service.LocationWebSocketService;
import com.example.emergencyassistb4b4.location.service.LocationService;
import com.example.emergencyassistb4b4.attendance.socket.notifier.TrackingNotifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class LocationTrackingWebSocketHandler implements WebSocketHandler {

    private final LocationService locationService;
    private final LocationWebSocketService locationWebSocketService;
    private final TrackingNotifier trackingNotifier;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("웹소켓 연결됨, sessionId={}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String payload = textMessage.getPayload();
                // JSON -> LocationUpdateMessage 파싱
                WebSocketMessageWrapper wrapper = objectMapper.readValue(payload, WebSocketMessageWrapper.class);

                LocationUpdateMessage locMsg = wrapper.getData();

                Long volunteerId = locMsg.getVolunteerId();
                double lat = locMsg.getLatitude();
                double lon = locMsg.getLongitude();

                log.debug("Received location update from volunteerId={} lat={} lon={}", volunteerId, lat, lon);

                // 1. Redis GEO 저장
                locationService.saveCoordinates(volunteerId, lat, lon);

                // 2. 출석 체크 수행
                boolean isPresent = locationWebSocketService.checkAttendanceForVolunteer(volunteerId, lat, lon);

                // 3. 출석 상태 웹소켓으로 알림 전송
                trackingNotifier.notifyTrackingCheck(volunteerId, isPresent);

                // 4. redis 에 저장 및 publish
                locationWebSocketService.saveAndPublishAttendance(volunteerId, isPresent);
            } else {
                log.warn("지원하지 않는 메시지 타입: {}", message.getClass());
            }
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("전송 오류 발생", exception);
        try {
            if (session.isOpen()) session.close(CloseStatus.SERVER_ERROR);
        } catch (Exception e) {
            log.error("세션 닫기 실패", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("웹소켓 연결 종료, sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
