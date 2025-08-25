package com.example.emergencyassistb4b4.domain.attendance.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisRepository redisRepository;

    private static final String TRACKING_STARTED_VALUE = "TRACKING_STARTED";
    private static final Duration TEAM_TRACKING_TTL = Duration.ofMinutes(30);


    // === RabbitMQ 상태 ===
    public void setRabbitMQState(Long teamId) {
        redisRepository.setRabbitMQState(teamId);
    }

    public void changeRabbitMQState(Long teamId) {
        redisRepository.changeRabbitMQState(teamId);
    }

    public String getRabbitmqState(Long teamId){
        return redisRepository.getRabbitmqState(teamId);
    }

    public void deleteRabbitMQState(Long teamId) {
        redisRepository.deleteRabbitMQState(teamId);
    }

    // ===== 팀 출석 상태 =====
    public void cacheTeamTrackingStart(Long teamId) {
        redisRepository.setTeamTrackingState(teamId, TRACKING_STARTED_VALUE, TEAM_TRACKING_TTL.getSeconds());
    }

    public boolean isTrackingInProgress(Long teamId) {
        String state = redisRepository.getTeamTrackingState(teamId);
        return TRACKING_STARTED_VALUE.equals(state);
    }

    public void clearTrackingStatus(Long teamId) {
        redisRepository.deleteTeamTrackingState(teamId);
    }

    // ===== 자원봉사자 → 팀ID 매핑 =====
    public void cacheTeamIdForVolunteer(Long volunteerId, Long teamId) {
        redisRepository.cacheTeamIdForVolunteer(volunteerId, teamId, TEAM_TRACKING_TTL);
    }

    public Long getTeamIdForVolunteer(Long volunteerId) {
        return redisRepository.getTeamIdForVolunteer(volunteerId);
    }

    public void deleteTeamIdForVolunteer(Long volunteerId) {
        redisRepository.deleteTeamIdForVolunteer(volunteerId);
    }

    // ===== 팀 위치 GEO =====
    public void addTeamGeoLocation(Long teamId, double lat, double lon) {
        redisRepository.addTeamGeoLocation(teamId, lat, lon);
    }

    public boolean hasGeoKey(Long teamId) {
        return redisRepository.hasGeoKey(teamId);
    }

    public boolean radiusSearch(Long teamId, double lat, double lon, int radiusMeters) {
        return redisRepository.radiusSearch(teamId, lat, lon, radiusMeters);
    }

    // ===== 출석 세션 기록 =====
    public void saveAttendanceRecord(Long volunteerId, boolean isPresent) {
        redisRepository.saveAttendanceRecord(volunteerId, isPresent);
    }

    public List<String> getAttendanceRecords(Long volunteerId) {
        return redisRepository.getAttendanceRecords(volunteerId);
    }

    public void deleteAttendanceRecords(Long volunteerId) {
        redisRepository.deleteAttendanceRecords(volunteerId);
    }
}
