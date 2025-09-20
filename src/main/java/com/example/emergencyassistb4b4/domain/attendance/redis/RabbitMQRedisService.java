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

    // ---------------- RabbitMQ 상태 ----------------

    // 출석 예약 상태 저장
    public void scheduleTrackingStart(Long teamId, LocalDateTime joinedAt) {
        if (joinedAt == null || joinedAt.isBefore(LocalDateTime.now())) {
            log.warn("Invalid joinedAt for teamId {}: {}", teamId, joinedAt);
            return;
        }
        rabbitMQRedisRepository.saveRabbitMQState(teamId, joinedAt);
    }

    // 출석 상태 업데이트
    public void updateTrackingState(Long teamId, LocalDateTime joinedAt) {
        if (joinedAt == null || joinedAt.isBefore(LocalDateTime.now())) {
            log.warn("Attempted to update past joinedAt for teamId {}: {}", teamId, joinedAt);
            return;
        }
        rabbitMQRedisRepository.updateRabbitMQState(teamId, joinedAt);
    }

    // 출석 상태 조회
    public RabbitMQ getTrackingState(Long teamId) {
        return rabbitMQRedisRepository.getRabbitMQState(teamId);
    }

    // 모든 트래킹 상태 팀 ID 조회
    public List<Long> getAllTrackingStates() {
        return rabbitMQRedisRepository.getAllTrackingStates().stream().toList();
    }

    // 출석 상태 삭제
    public void clearTrackingState(Long teamId) {
        rabbitMQRedisRepository.deleteRabbitMQState(teamId);
    }

    // ---------------- 자원봉사자 - 유저 매핑 ----------------

    // 자원봉사자와 유저 매핑
    public void mapVolunteerToUser(Long volunteerId, Long userId) {
        rabbitMQRedisRepository.cacheUserIdForVolunteer(volunteerId, userId);
    }

    // 자원봉사자로 유저 조회
    public Long findUserIdByVolunteer(Long volunteerId) {
        return rabbitMQRedisRepository.getUserIdForVolunteer(volunteerId);
    }

    // 자원봉사자와 유저 매핑 제거
    public void unmapVolunteerFromUser(Long volunteerId) {
        rabbitMQRedisRepository.deleteUserIdForVolunteer(volunteerId);
    }

    // ---------------- 팀 위치 ----------------

    // 팀 위치 갱신
    public void updateTeamLocation(Long teamId, double lat, double lon, Duration ttl) {
        rabbitMQRedisRepository.addTeamGeoLocation(teamId, lat, lon, ttl);
    }

    // 팀 위치 존재 여부 확인
    public boolean locationExists(Long teamId) {
        return rabbitMQRedisRepository.hasGeoKey(teamId);
    }

    // 팀 위치 반경 확인
    public boolean isWithinRadius(Long teamId, double lat, double lon, int radiusMeters) {
        return rabbitMQRedisRepository.radiusSearch(teamId, lat, lon, radiusMeters);
    }

    // ---------------- 자원봉사자 - 팀 매핑 ----------------

    // 자원봉사자와 팀 매핑
    public void mapVolunteerToTeam(Long volunteerId, Long teamId) {
        rabbitMQRedisRepository.mapVolunteerToTeam(volunteerId, teamId);
    }

    // 자원봉사자로 팀 조회
    public Long findTeamByVolunteer(Long volunteerId) {
        return rabbitMQRedisRepository.findTeamByVolunteer(volunteerId);
    }

    // ---------------- 출석 세션 ----------------

    // 출석 기록 저장
    public void recordAttendance(Long volunteerId, boolean isPresent, Duration ttl) {
        rabbitMQRedisRepository.saveAttendanceRecord(volunteerId, isPresent, ttl);
    }

    // 출석 기록 조회
    public List<String> fetchAttendanceRecords(Long volunteerId) {
        return rabbitMQRedisRepository.fetchAttendanceRecords(volunteerId).stream()
                .map(Object::toString)
                .toList();
    }

    // 출석 기록 삭제
    public void clearAttendanceHistory(Long volunteerId) {
        rabbitMQRedisRepository.deleteAttendanceRecords(volunteerId);
    }
}
