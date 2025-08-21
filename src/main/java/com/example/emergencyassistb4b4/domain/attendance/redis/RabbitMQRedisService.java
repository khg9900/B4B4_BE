package com.example.emergencyassistb4b4.domain.attendance.redis;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.RabbitMQ;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQRedisService {

    private final RabbitMQRedisRepository rabbitMQRedisRepository;

    private static final String TRACKING_STARTED = "TRACKING_STARTED";
    private static final Duration TTL = Duration.ofMinutes(30);

    // ======================== RabbitMQ 상태 ========================

    public void scheduleTrackingStart(Long teamId, LocalDateTime joinedAt) {
        if (joinedAt == null || joinedAt.isBefore(LocalDateTime.now())) {
            log.warn("Invalid joinedAt for teamId {}: {}", teamId, joinedAt);
            return;
        }
        rabbitMQRedisRepository.saveRabbitMQState(teamId, joinedAt);
    }

    public void updateTrackingState(Long teamId, LocalDateTime joinedAt) {
        if (joinedAt == null || joinedAt.isBefore(LocalDateTime.now())) {
            log.warn("Attempted to update past joinedAt for teamId {}: {}", teamId, joinedAt);
            return;
        }
        rabbitMQRedisRepository.updateRabbitMQState(teamId, joinedAt);
    }

    public RabbitMQ getTrackingState(Long teamId) {
        return rabbitMQRedisRepository.getRabbitMQState(teamId);
    }

    public List<Long> getAllTrackingStates() {
        return rabbitMQRedisRepository.getAllTrackingStates().stream()
                .toList();
    }



    public void clearTrackingState(Long teamId) {
        rabbitMQRedisRepository.deleteRabbitMQState(teamId);
    }

    // ======================== 출석 상태 ========================

    public void markTrackingStarted(Long teamId) {
        rabbitMQRedisRepository.setTeamTrackingState(teamId, TRACKING_STARTED, TTL.getSeconds());
    }

    public boolean isTrackingOngoing(Long teamId) {
        return TRACKING_STARTED.equals(rabbitMQRedisRepository.getTeamTrackingState(teamId));
    }

    public void clearTrackingStatus(Long teamId) {
        rabbitMQRedisRepository.deleteTeamTrackingState(teamId);
    }

    // ======================== 자원봉사자 - 유저 매핑 ========================

    public void mapVolunteerToUser(Long volunteerId, Long userId) {
        rabbitMQRedisRepository.cacheUserIdForVolunteer(volunteerId, userId);
    }

    public Long findUserIdByVolunteer(Long volunteerId) {
        return rabbitMQRedisRepository.getUserIdForVolunteer(volunteerId);
    }

    public void unmapVolunteerFromUser(Long volunteerId) {
        rabbitMQRedisRepository.deleteUserIdForVolunteer(volunteerId);
    }


    // ======================== 팀 위치 ========================

    public void updateTeamLocation(Long teamId, double lat, double lon, Duration ttl) {
        rabbitMQRedisRepository.addTeamGeoLocation(teamId, lat, lon, ttl );
    }

    public boolean locationExists(Long teamId) {
        return rabbitMQRedisRepository.hasGeoKey(teamId);
    }

    public boolean isWithinRadius(Long teamId, double lat, double lon, int radiusMeters) {
        return rabbitMQRedisRepository.radiusSearch(teamId, lat, lon, radiusMeters);
    }

    // ======================== 자원봉사자 - 팀 매핑 ========================
    public void mapVolunteerToTeam(Long volunteerId, Long teamId) {
        rabbitMQRedisRepository.mapVolunteerToTeam(volunteerId, teamId);
    }

    public Long findTeamByVolunteer(Long volunteerId) {
        return rabbitMQRedisRepository.findTeamByVolunteer(volunteerId);
    }

    // ======================== 출석 세션 ========================

    public void recordAttendance(Long volunteerId, boolean isPresent, Duration ttl) {
        rabbitMQRedisRepository.saveAttendanceRecord(volunteerId, isPresent,ttl);
    }

    public List<String> fetchAttendanceRecords(Long volunteerId) {
        return rabbitMQRedisRepository.fetchAttendanceRecords(volunteerId).stream()
                .map(Object::toString)
                .toList();
    }

    public void clearAttendanceHistory(Long volunteerId) {
        rabbitMQRedisRepository.deleteAttendanceRecords(volunteerId);
    }
}
