package com.example.emergencyassistb4b4.domain.attendance.socket.service;

import com.example.emergencyassistb4b4.domain.attendance.socket.dto.LocationUpdateMessage;
import com.example.emergencyassistb4b4.domain.attendance.socket.dto.WebSocketMessageWrapper;
import com.example.emergencyassistb4b4.domain.attendance.socket.utils.WebSocketUtils;
import com.example.emergencyassistb4b4.domain.location.service.LocationService;
import com.example.emergencyassistb4b4.domain.attendance.socket.notifier.TrackingNotifier;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationTrackingService {

    private final LocationService locationService;
    private final LocationWebSocketService locationWebSocketService;
    private final TrackingNotifier trackingNotifier;
    private final ObjectMapper objectMapper;

    /**
     * 수신된 웹소켓 메시지를 처리하고,
     * 1) 위치 정보를 Redis GEO에 저장
     * 2) 출석 체크 수행
     * 3) 출석 상태 알림 전송
     * 4) Redis 저장 및 publish 수행
     *
     * @param payload 웹소켓에서 전달된 메시지 JSON 문자열
     */
    public void processMessage(String payload) {
        WebSocketMessageWrapper wrapper;
        try {
            wrapper = objectMapper.readValue(payload, WebSocketMessageWrapper.class);
        } catch (Exception e) {
            return; // JSON 파싱 실패
        }

        LocationUpdateMessage locMsg = wrapper.getData();
        if (locMsg == null) return;

        Long volunteerId = locMsg.getVolunteerId();
        double lat = locMsg.getLatitude();
        double lon = locMsg.getLongitude();
        if (volunteerId == null || !WebSocketUtils.isValidCoordinates(lat, lon)) return;

        // 1. Redis GEO 저장
        locationService.saveCoordinates(volunteerId, lat, lon);

        // 2. 출석 체크
        boolean isPresent = locationWebSocketService.checkAttendanceForVolunteer(volunteerId, lat, lon);

        // 3. 알림 전송
        trackingNotifier.notifyTrackingCheck(volunteerId, isPresent);

        // 4. Redis 저장 및 publish
        locationWebSocketService.saveAndPublishAttendance(volunteerId, isPresent);
    }
}
