package com.example.emergencyassistb4b4.domain.attendance.service;

import com.example.emergencyassistb4b4.domain.attendance.dto.MessageWrapper;
import com.example.emergencyassistb4b4.domain.attendance.dto.SessionState;
import com.example.emergencyassistb4b4.domain.attendance.dto.TrackingSessionDto;
import com.example.emergencyassistb4b4.domain.attendance.publisher.TrackingSessionPublisher;
import com.example.emergencyassistb4b4.domain.volunteer.domain.*;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerTeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.emergencyassistb4b4.global.status.ErrorStatus.ATTENDANCE_LOCATION_OR_POLICY_MISSING;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final TrackingSessionPublisher trackingSessionPublisher;
    private final VolunteerTeamRepository volunteerTeamRepository;

    /**
     * 팀에 대한 위치 추적 세션 예약 시작
     */
    public void scheduleTrackingForTeam(Long teamId) {
        VolunteerTeam team = volunteerTeamRepository.findWithPostAndDetailsById(teamId)
                .orElseThrow();

        Post post = team.getPost();
        VolunteerLocation location = post.getLocation();
        AttendancePolicy policy = post.getAttendancePolicy();

        if (location == null || policy == null) {
            throw new ApiException(ATTENDANCE_LOCATION_OR_POLICY_MISSING);
        }

        List<Long> participantUserIds = team.getParticipants().stream()
                .filter(participant -> participant.getCheckinStatus() == CheckinStatus.PARTICIPATED)
                .map(VolunteerParticipant::getId)
                .toList();

        TrackingSessionDto sessionDto = TrackingSessionDto.from(team, location, policy, participantUserIds);

        LocalDateTime checkinStart = policy.getCheckinStart();

        // 1. READY 메시지 예약 (출석 시작 1분 전)
        LocalDateTime readyTime = checkinStart.minusMinutes(1);
        scheduleTrackingAtTime(new MessageWrapper(SessionState.READY, sessionDto), readyTime);

        // 2. STARTED 메시지 예약 (출석 시작 시점)
        scheduleTrackingAtTime(new MessageWrapper(SessionState.STARTED, sessionDto), checkinStart);

        // 3. ENDED 메시지 예약 (출석 시작 30분 뒤)
        LocalDateTime endTime = checkinStart.plusMinutes(30);
        scheduleTrackingAtTime(new MessageWrapper(SessionState.ENDED, sessionDto), endTime);

        log.debug("Tracking session scheduled: teamId={}, ready={}, start={}, end={}",
                teamId, readyTime, checkinStart, endTime);
    }


    /**
     * 메시지를 일정 시간 뒤에 발행
     */
    public void scheduleTrackingAtTime(MessageWrapper wrapper, LocalDateTime scheduledTime) {
        long delayMillis = Duration.between(LocalDateTime.now(), scheduledTime).toMillis();

        if (delayMillis < 0) {
            log.warn("Scheduled time {} is in the past. Sending immediately.", scheduledTime);
            delayMillis = 0;
        }

        trackingSessionPublisher.publishDelayedTrackingSession(wrapper, delayMillis);
    }
}
