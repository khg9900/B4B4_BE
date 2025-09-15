package com.example.emergencyassistb4b4.domain.attendance.socket.controller;

import com.example.emergencyassistb4b4.domain.attendance.socket.dto.LocationUpdateMessage;
import com.example.emergencyassistb4b4.domain.attendance.socket.dto.WebSocketMessageWrapper;
import com.example.emergencyassistb4b4.domain.attendance.socket.service.LocationWebSocketService;
import com.example.emergencyassistb4b4.domain.location.service.LocationService;
import com.example.emergencyassistb4b4.domain.attendance.socket.notifier.TrackingNotifier;
import com.fasterxml.jackson.core.JsonProcessingException;
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
        if (!(message instanceof TextMessage textMessage)) {
            log.warn("지원하지 않는 메시지 타입: {}", message.getClass());
            return;
        }

        try {
            processLocationMessage(textMessage.getPayload());
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생", e);
        }
    }

    private void processLocationMessage(String payload) {
        WebSocketMessageWrapper wrapper;
        try {
            wrapper = objectMapper.readValue(payload, WebSocketMessageWrapper.class);
        } catch (JsonProcessingException e) {
            log.warn("JSON 파싱 실패, payload={}", payload, e);
            return;
        }

        LocationUpdateMessage locMsg = wrapper.getData();
        if (locMsg == null) {
            log.warn("빈 데이터 수신, payload={}", payload);
            return;
        }

        Long volunteerId = locMsg.getVolunteerId();
        double lat = locMsg.getLatitude();
        double lon = locMsg.getLongitude();

        if (volunteerId == null || !isValidCoordinates(lat, lon)) {
            log.warn("비정상 데이터 수신, volunteerId={}, lat={}, lon={}", volunteerId, lat, lon);
            return;
        }

        log.debug("Received location update from volunteerId={} lat={} lon={}", volunteerId, lat, lon);

        // 1. Redis GEO 저장
        locationService.saveCoordinates(volunteerId, lat, lon);

        // 2. 출석 체크 수행
        boolean isPresent = locationWebSocketService.checkAttendanceForVolunteer(volunteerId, lat, lon);

        // 3. 출석 상태 웹소켓 알림 전송
        trackingNotifier.notifyTrackingCheck(volunteerId, isPresent);

        // 4. Redis 저장 및 publish
        locationWebSocketService.saveAndPublishAttendance(volunteerId, isPresent);
    }

    private boolean isValidCoordinates(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("전송 오류 발생", exception);
        closeSessionSilently(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("웹소켓 연결 종료, sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void closeSessionSilently(WebSocketSession session) {
        try {
            if (session.isOpen()) session.close(CloseStatus.SERVER_ERROR);
        } catch (Exception e) {
            log.error("세션 닫기 실패", e);
        }
    }
}
