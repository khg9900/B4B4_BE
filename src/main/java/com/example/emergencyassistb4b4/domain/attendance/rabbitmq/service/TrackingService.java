package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.service;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.MessageWrapper;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.SessionState;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.TrackingSessionDto;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.publisher.TrackingSessionPublisher;
import com.example.emergencyassistb4b4.domain.attendance.socket.handler.TrackingSocketHandler;
import com.example.emergencyassistb4b4.domain.volunteer.domain.*;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerTeamRepository;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
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
    private final TrackingSocketHandler trackingSocketHandler;
    /**
     * 팀에 대한 위치 추적 세션 예약 시작
     */
    public void scheduleTrackingForTeam(Long teamId) {
        VolunteerTeam team = volunteerTeamRepository.findWithPostAndDetailsById(teamId)
                .orElseThrow(()->new ApiException(ErrorStatus.TEAM_NOT_FOUND));

        Post post = team.getPost();
        VolunteerLocation location = post.getLocation();
        AttendancePolicy policy = post.getAttendancePolicy();

        if (location == null || policy == null) {
            throw new ApiException(ATTENDANCE_LOCATION_OR_POLICY_MISSING);
        }

        List<VolunteerParticipant> participants = team.getParticipants().stream()
                .filter(participant -> participant.getCheckinStatus() == CheckinStatus.PARTICIPATED)
                .toList();

        List<Long> participantUserIds = participants.stream()
                .map(VolunteerParticipant::getId)
                .toList();

        participants.forEach(p -> trackingSocketHandler.cacheVolunteerUserMapping(p.getId(), p.getUser().getId()));


        TrackingSessionDto sessionDto = TrackingSessionDto.from(team, location, policy, participantUserIds);

        LocalDateTime checkinStart = policy.getCheckinStart();


        // 1. READY 메시지 예약 (출석 시작 1분 전)
        LocalDateTime readyTime = checkinStart.minusMinutes(1);
        scheduleTrackingAtTime(new MessageWrapper(SessionState.READY, sessionDto), readyTime);

        // 2. STARTED 메시지 예약 (출석 시작 시점)
        scheduleTrackingAtTime(new MessageWrapper(SessionState.STARTED, sessionDto), checkinStart);

        // 3. ENDED 메시지 예약 (출석 종료 시점)
        LocalDateTime endTime = policy.getCheckinEnd();
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
