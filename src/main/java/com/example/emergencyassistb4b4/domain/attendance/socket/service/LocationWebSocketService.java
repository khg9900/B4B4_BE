package com.example.emergencyassistb4b4.domain.attendance.socket.service;

import com.example.emergencyassistb4b4.domain.volunteer.domain.*;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
import com.example.emergencyassistb4b4.domain.attendance.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;

import java.time.LocalDateTime;

import static com.example.emergencyassistb4b4.global.status.ErrorStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationWebSocketService {

    private final VolunteerParticipantRepository volunteerParticipantRepository;
    private final RedisService redisService;

    private static final int DEFAULT_RADIUS_METERS = 50;

    public boolean checkAttendanceForVolunteer(Long volunteerId, double lat, double lon) {
        Long teamId = redisService.getTeamIdForVolunteer(volunteerId);
        VolunteerParticipant participant = null;

        if (teamId == null) {
            participant = volunteerParticipantRepository
                    .findWithTeamAndPolicyById(volunteerId)
                    .orElseThrow(() -> new ApiException(VOLUNTEER_NOT_FOUND));

            teamId = participant.getVolunteerTeam().getId();
            redisService.cacheTeamIdForVolunteer(volunteerId, teamId);
            log.debug("Cached teamId={} for volunteerId={}", teamId, volunteerId);
        }

        if (redisService.hasGeoKey(teamId)) {
            return redisService.radiusSearch(teamId, lat, lon, DEFAULT_RADIUS_METERS);
        }

        if (participant == null) {
            participant = volunteerParticipantRepository
                    .findWithTeamAndPolicyById(volunteerId)
                    .orElseThrow(() -> new ApiException(VOLUNTEER_NOT_FOUND));
        }

        VolunteerTeam team = participant.getVolunteerTeam();
        Post post = team.getPost();
        VolunteerLocation location = post.getLocation();
        AttendancePolicy policy = post.getAttendancePolicy();

        if (location == null || policy == null) {
            throw new ApiException(ATTENDANCE_LOCATION_OR_POLICY_MISSING);
        }

        redisService.addTeamGeoLocation(teamId, location.getLocationLat(), location.getLocationLng());

        return redisService.radiusSearch(teamId, lat, lon, policy.getAttendanceRadiusMeters());
    }

    public void saveAndPublishAttendance(Long volunteerId, boolean isPresent) {
        redisService.saveAttendanceRecord(volunteerId, isPresent);
    }
}
