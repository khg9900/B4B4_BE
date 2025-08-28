package com.example.emergencyassistb4b4.domain.attendance.socket.service;

import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import com.example.emergencyassistb4b4.domain.volunteer.domain.*;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.example.emergencyassistb4b4.global.status.ErrorStatus.ATTENDANCE_LOCATION_OR_POLICY_MISSING;
import static com.example.emergencyassistb4b4.global.status.ErrorStatus.VOLUNTEER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationWebSocketService {

    private final VolunteerParticipantRepository volunteerParticipantRepository;
    private final RabbitMQRedisService rabbitMQRedisService;

    private static final int DEFAULT_RADIUS_METERS = 50;
    private static final int DEFAULT_TTL_MINUTES = 3; // 여유 시간

    public boolean checkAttendanceForVolunteer(Long volunteerId, double lat, double lon) {
        // 1️⃣ 팀 ID 조회/캐싱
        VolunteerParticipant participant = volunteerParticipantRepository
                .findWithTeamAndPolicyById(volunteerId)
                .orElseThrow(() -> new ApiException(VOLUNTEER_NOT_FOUND));

        Long teamId = rabbitMQRedisService.findTeamByVolunteer(volunteerId);
        if (teamId == null) {
            teamId = participant.getVolunteerTeam().getId();
            rabbitMQRedisService.mapVolunteerToTeam(volunteerId, teamId);
            log.debug("Cached teamId={} for volunteerId={}", teamId, volunteerId);
        }

        // 2️⃣ 팀 위치 확인/캐싱
        if (!rabbitMQRedisService.locationExists(teamId)) {
            VolunteerTeam team = participant.getVolunteerTeam();
            Post post = team.getPost();
            VolunteerLocation location = post.getLocation();
            AttendancePolicy policy = post.getAttendancePolicy();

            if (location == null || policy == null) {
                throw new ApiException(ATTENDANCE_LOCATION_OR_POLICY_MISSING);
            }

            // TTL = 세션 종료 시간까지 + 여유 3분
            Duration ttl = Duration.between(LocalDateTime.now(), policy.getCheckinEnd()).plusMinutes(DEFAULT_TTL_MINUTES);
            if (ttl.isNegative() || ttl.isZero()) ttl = Duration.ofMinutes(DEFAULT_TTL_MINUTES);

            rabbitMQRedisService.updateTeamLocation(teamId, location.getLocationLat(), location.getLocationLng(), ttl);
            log.debug("Cached geo center for teamId={} at lat={}, lon={}, ttl={}s", teamId, location.getLocationLat(), location.getLocationLng(), ttl.getSeconds());
        }

        // 3️⃣ 반경 체크
        int radius = participant.getVolunteerTeam().getPost().getAttendancePolicy() != null
                ? participant.getVolunteerTeam().getPost().getAttendancePolicy().getAttendanceRadiusMeters()
                : DEFAULT_RADIUS_METERS;

        boolean withinRadius = rabbitMQRedisService.isWithinRadius(teamId, lat, lon, radius);
        log.debug("Geo check for teamId={} with lat={}, lon={}, radius={}m: {}", teamId, lat, lon, radius, withinRadius);

        return withinRadius;
    }

    public void saveAndPublishAttendance(Long volunteerId, boolean isPresent) {
        VolunteerParticipant participant = volunteerParticipantRepository
                .findWithTeamAndPolicyById(volunteerId)
                .orElseThrow(() -> new ApiException(VOLUNTEER_NOT_FOUND));

        AttendancePolicy policy = participant.getVolunteerTeam().getPost().getAttendancePolicy();

        // ENDED 이벤트 처리 시, 현재 시점과 sessionEnd 중 더 늦은 시간을 기준
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionEnd = (policy != null && policy.getCheckinEnd() != null)
                ? policy.getCheckinEnd()
                : now;

        // TTL 계산: sessionEnd 이후 DEFAULT_TTL_MINUTES까지
        LocalDateTime effectiveEnd = sessionEnd.isAfter(now) ? sessionEnd : now;
        Duration ttl = Duration.between(now, effectiveEnd).plusMinutes(DEFAULT_TTL_MINUTES);

        // 음수 방어
        if (ttl.isNegative() || ttl.isZero()) ttl = Duration.ofMinutes(DEFAULT_TTL_MINUTES);

        rabbitMQRedisService.recordAttendance(volunteerId, isPresent, ttl);
        log.info("Saved attendance for volunteerId={}, isPresent={}, ttl={}s", volunteerId, isPresent, ttl.getSeconds());
    }
}
