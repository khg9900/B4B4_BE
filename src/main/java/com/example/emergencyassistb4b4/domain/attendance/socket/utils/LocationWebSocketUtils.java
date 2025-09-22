package com.example.emergencyassistb4b4.domain.attendance.socket.utils;

import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import com.example.emergencyassistb4b4.domain.volunteer.domain.*;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
import com.example.emergencyassistb4b4.global.exception.ApiException;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.example.emergencyassistb4b4.global.status.ErrorStatus.ATTENDANCE_LOCATION_OR_POLICY_MISSING;

public class LocationWebSocketUtils {

    // ================== 팀 캐싱 관련 ==================

    //자원봉사자의 팀 ID를 Redis에서 조회하거나 없으면 캐싱
    public static Long getOrCacheTeamId(Long volunteerId, VolunteerParticipant participant,
                                        RabbitMQRedisService rabbitMQRedisService) {
        Long teamId = rabbitMQRedisService.findTeamByVolunteer(volunteerId);
        if (teamId == null) {
            teamId = participant.getVolunteerTeam().getId();
            rabbitMQRedisService.mapVolunteerToTeam(volunteerId, teamId);
        }
        return teamId;
    }

    //팀 위치가 Redis에 캐싱되어 있는지 확인하고, 없으면 캐싱
    public static void ensureTeamLocationCached(VolunteerParticipant participant, Long teamId,
                                                RabbitMQRedisService rabbitMQRedisService) {
        if (rabbitMQRedisService.locationExists(teamId)) return;

        VolunteerTeam team = participant.getVolunteerTeam();
        Post post = team.getPost();
        VolunteerLocation location = post.getLocation();
        AttendancePolicy policy = post.getAttendancePolicy();

        if (location == null || policy == null) throw new ApiException(ATTENDANCE_LOCATION_OR_POLICY_MISSING);

        Duration ttl = computeTTL(policy);
        rabbitMQRedisService.updateTeamLocation(teamId, location.getLocationLat(), location.getLocationLng(), ttl);
    }

    // ================== 출석 반경/거리 관련 ==================

    //자원봉사자의 출석 정책에서 출석 반경을 가져오거나, 없으면 기본값 사용
    public static int getAttendanceRadius(VolunteerParticipant participant, int defaultRadius) {
        AttendancePolicy policy = participant.getVolunteerTeam().getPost().getAttendancePolicy();
        return policy != null ? policy.getAttendanceRadiusMeters() : defaultRadius;
    }

    //특정 좌표가 팀 중심 위치에서 지정 반경 내에 있는지 확인
    public static boolean isWithinRadius(Long teamId, double lat, double lon, int radius,
                                         RabbitMQRedisService rabbitMQRedisService) {
        return rabbitMQRedisService.isWithinRadius(teamId, lat, lon, radius);
    }

    // ================== TTL 계산 ==================

    //자원봉사자 ID 기반으로 출석 TTL 계산
    public static Duration computeTTL(Long volunteerId, VolunteerParticipantRepository participantRepository) {
        VolunteerParticipant participant = participantRepository.findWithTeamAndPolicyById(volunteerId)
                .orElseThrow();
        AttendancePolicy policy = participant.getVolunteerTeam().getPost().getAttendancePolicy();
        return computeTTL(policy);
    }

    //출석 정책 기준 TTL 계산
    public static Duration computeTTL(AttendancePolicy policy) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionEnd = (policy != null && policy.getCheckinEnd() != null) ? policy.getCheckinEnd() : now;
        Duration ttl = Duration.between(now, sessionEnd.isAfter(now) ? sessionEnd : now).plusMinutes(3);
        return ttl.isNegative() || ttl.isZero() ? Duration.ofMinutes(3) : ttl;
    }
}
