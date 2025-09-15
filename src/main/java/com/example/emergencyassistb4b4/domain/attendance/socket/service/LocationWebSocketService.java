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

    private final VolunteerParticipantRepository participantRepository;
    private final RabbitMQRedisService rabbitMQRedisService;

    private static final int DEFAULT_RADIUS_METERS = 50;
    private static final int DEFAULT_TTL_MINUTES = 3;

    /**
     * 해당 자원봉사자가 팀 위치 반경 내에 있는지 체크
     */
    public boolean checkAttendanceForVolunteer(Long volunteerId, double lat, double lon) {
        VolunteerParticipant participant = participantRepository
                .findWithTeamAndPolicyById(volunteerId)
                .orElseThrow(() -> new ApiException(VOLUNTEER_NOT_FOUND));

        Long teamId = rabbitMQRedisService.findTeamByVolunteer(volunteerId);
        if (teamId == null) {
            teamId = participant.getVolunteerTeam().getId();
            rabbitMQRedisService.mapVolunteerToTeam(volunteerId, teamId);
            log.debug("Cached teamId={} for volunteerId={}", teamId, volunteerId);
        }

        cacheTeamLocationIfAbsent(participant, teamId);

        int radius = participant.getVolunteerTeam().getPost().getAttendancePolicy() != null
                ? participant.getVolunteerTeam().getPost().getAttendancePolicy().getAttendanceRadiusMeters()
                : DEFAULT_RADIUS_METERS;

        boolean withinRadius = rabbitMQRedisService.isWithinRadius(teamId, lat, lon, radius);
        log.debug("Geo check for teamId={} lat={}, lon={}, radius={}m: {}", teamId, lat, lon, radius, withinRadius);

        return withinRadius;
    }

    private void cacheTeamLocationIfAbsent(VolunteerParticipant participant, Long teamId) {
        if (rabbitMQRedisService.locationExists(teamId)) return;

        VolunteerTeam team = participant.getVolunteerTeam();
        Post post = team.getPost();
        VolunteerLocation location = post.getLocation();
        AttendancePolicy policy = post.getAttendancePolicy();

        if (location == null || policy == null) {
            throw new ApiException(ATTENDANCE_LOCATION_OR_POLICY_MISSING);
        }

        Duration ttl = Duration.between(LocalDateTime.now(), policy.getCheckinEnd())
                .plusMinutes(DEFAULT_TTL_MINUTES);
        if (ttl.isNegative() || ttl.isZero()) ttl = Duration.ofMinutes(DEFAULT_TTL_MINUTES);

        rabbitMQRedisService.updateTeamLocation(teamId, location.getLocationLat(), location.getLocationLng(), ttl);
        log.debug("Cached geo center for teamId={} at lat={}, lon={}, ttl={}s", teamId, location.getLocationLat(), location.getLocationLng(), ttl.getSeconds());
    }

    /**
     * Redis에 출석 기록 저장 및 publish
     */
    public void saveAndPublishAttendance(Long volunteerId, boolean isPresent) {
        VolunteerParticipant participant = participantRepository
                .findWithTeamAndPolicyById(volunteerId)
                .orElseThrow(() -> new ApiException(VOLUNTEER_NOT_FOUND));

        AttendancePolicy policy = participant.getVolunteerTeam().getPost().getAttendancePolicy();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionEnd = (policy != null && policy.getCheckinEnd() != null)
                ? policy.getCheckinEnd()
                : now;

        Duration ttl = Duration.between(now, sessionEnd.isAfter(now) ? sessionEnd : now)
                .plusMinutes(DEFAULT_TTL_MINUTES);
        if (ttl.isNegative() || ttl.isZero()) ttl = Duration.ofMinutes(DEFAULT_TTL_MINUTES);

        rabbitMQRedisService.recordAttendance(volunteerId, isPresent, ttl);
        log.info("Saved attendance: volunteerId={}, isPresent={}, ttl={}s", volunteerId, isPresent, ttl.getSeconds());
    }
}
