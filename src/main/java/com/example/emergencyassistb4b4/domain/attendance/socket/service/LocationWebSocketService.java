package com.example.emergencyassistb4b4.domain.attendance.socket.service;

import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import com.example.emergencyassistb4b4.domain.attendance.socket.utils.LocationWebSocketUtils;
import com.example.emergencyassistb4b4.domain.volunteer.domain.*;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.domain.attendance.socket.message.AttendanceStatusMessage;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.example.emergencyassistb4b4.global.status.ErrorStatus.VOLUNTEER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LocationWebSocketService {

    private final VolunteerParticipantRepository participantRepository;
    private final RabbitMQRedisService rabbitMQRedisService;

    private static final int DEFAULT_RADIUS_METERS = 50;

    // ================== 출석 체크 관련 ==================

    //자원봉사자 위치 기반 출석 체크
    public boolean checkAttendanceForVolunteer(Long volunteerId, double lat, double lon) {
        VolunteerParticipant participant = getParticipantOrThrow(volunteerId);
        Long teamId = LocationWebSocketUtils.getOrCacheTeamId(volunteerId, participant, rabbitMQRedisService);
        LocationWebSocketUtils.ensureTeamLocationCached(participant, teamId, rabbitMQRedisService);
        int radius = LocationWebSocketUtils.getAttendanceRadius(participant, DEFAULT_RADIUS_METERS);
        return LocationWebSocketUtils.isWithinRadius(teamId, lat, lon, radius, rabbitMQRedisService);
    }

    // ================== 출석 기록 저장 ==================

    //Redis에 출석 기록 저장 및 publish
    public void saveAndPublishAttendance(Long volunteerId, boolean isPresent) {
        Duration ttl = LocationWebSocketUtils.computeTTL(volunteerId, participantRepository);
        rabbitMQRedisService.recordAttendance(volunteerId, isPresent, ttl);

        // DTO 생성 (WebSocket 전송용)
        AttendanceStatusMessage message = new AttendanceStatusMessage(volunteerId, isPresent);
    }

    // ================== Private Helpers ==================
    private VolunteerParticipant getParticipantOrThrow(Long volunteerId) {
        return participantRepository.findWithTeamAndPolicyById(volunteerId)
                .orElseThrow(() -> new ApiException(VOLUNTEER_NOT_FOUND));
    }
}
