package com.example.emergencyassistb4b4.domain.attendance.socket.service;

import com.example.emergencyassistb4b4.domain.volunteer.domain.*;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.example.emergencyassistb4b4.global.status.ErrorStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationWebSocketService {

    private final VolunteerParticipantRepository volunteerParticipantRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String GEO_KEY_PREFIX = "attendance:geo:team:";
    private static final String PARTICIPANT_KEY_PREFIX = "volunteer_user:";
    private static final String ATTENDANCE_SESSION_PREFIX = "attendance:session:";
    private static final int DEFAULT_RADIUS_METERS = 50;

    public boolean checkAttendanceForVolunteer(Long volunteerId, double lat, double lon) {
        String participantKey = PARTICIPANT_KEY_PREFIX + volunteerId;
        Long teamId = getTeamIdFromRedis(participantKey);
        VolunteerParticipant participant = null;

        if (teamId == null) {
            participant = volunteerParticipantRepository
                    .findWithTeamAndPolicyById(volunteerId)
                    .orElseThrow(() -> new ApiException(VOLUNTEER_NOT_FOUND));

            teamId = participant.getVolunteerTeam().getId();
            redisTemplate.opsForValue().set(participantKey, teamId.toString(), Duration.ofMinutes(30));
            log.debug("Cached teamId={} for volunteerId={}", teamId, volunteerId);
        }

        String geoKey = GEO_KEY_PREFIX + teamId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(geoKey))) {
            return isVolunteerWithinRadius(geoKey, lat, lon);
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

        Point postLocation = new Point(location.getLocationLng(), location.getLocationLat());
        redisTemplate.opsForGeo().add(geoKey, postLocation, "center");
        redisTemplate.expire(geoKey, Duration.ofMinutes(30));
        log.debug("Cached geo center for teamId={} at lat={}, lon={}", teamId, location.getLocationLat(), location.getLocationLng());

        return isVolunteerWithinRadius(geoKey, lat, lon, policy.getAttendanceRadiusMeters());
    }

    private Long getTeamIdFromRedis(String participantKey) {
        String value = redisTemplate.opsForValue().get(participantKey);
        if (value != null) {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException e) {
                log.warn("Invalid teamId format in Redis for key={}: {}", participantKey, value);
            }
        }
        return null;
    }

    private boolean isVolunteerWithinRadius(String geoKey, double lat, double lon) {
        return isVolunteerWithinRadius(geoKey, lat, lon, DEFAULT_RADIUS_METERS);
    }

    private boolean isVolunteerWithinRadius(String geoKey, double lat, double lon, int radius) {
        Point userLocation = new Point(lon, lat);
        Circle circle = new Circle(userLocation, new Distance(radius, Metrics.NEUTRAL));

        GeoResults<GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(geoKey, circle);

        boolean withinRadius = results.getContent().stream()
                .map(result -> result.getContent().getName())
                .anyMatch("center"::equals);

        log.debug("Geo check for key={} with lat={}, lon={}, radius={}m: {}", geoKey, lat, lon, radius, withinRadius);
        return withinRadius;
    }

    public void saveAndPublishAttendance(Long volunteerId, boolean isPresent) {
        String redisKey = ATTENDANCE_SESSION_PREFIX + volunteerId;
        String value = LocalDateTime.now() + ":" + (isPresent ? "1" : "0");
        redisTemplate.opsForList().rightPush(redisKey, value);
        log.debug("Saved attendance for volunteerId={}, value={}", volunteerId, value);
    }
}
