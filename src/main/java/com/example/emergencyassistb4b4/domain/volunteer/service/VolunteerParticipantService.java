package com.example.emergencyassistb4b4.domain.volunteer.service;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event.TrackingScheduleEvent;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event.TrackingScheduleEventListener;
import com.example.emergencyassistb4b4.domain.attendance.socket.handler.TrackingSocketHandler;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerTeam;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerTeamRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VolunteerParticipantService {

    private final UserRepository userRepository;
    private final VolunteerTeamRepository teamRepository;
    private final VolunteerParticipantRepository participantRepository;
    private final TrackingSocketHandler trackingSocketHandler;
    private final TrackingScheduleEventListener eventListener;

    @Transactional
    public VolunteerParticipant joinSave(Long userId, Long teamId) {
        // 유저 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND));

        // 팀 검증
        VolunteerTeam team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(ErrorStatus.VOLUNTEER_NOT_FOUND));

        // 팀 - 유저 정보 생성
        VolunteerParticipant participant = VolunteerParticipant.builder()
                .user(user)
                .volunteerTeam(team)
                .joinedAt(LocalDateTime.now())
                .checkinStatus(CheckinStatus.PARTICIPATED)
                .build();

        // 저장
        participant = participantRepository.save(participant);

        // WebSocket userId ↔ volunteerId 매핑
        trackingSocketHandler.cacheVolunteerUserMapping(participant.getId(), userId);

        // 출석 스케줄 이벤트 트리거
        eventListener.handleTrackingScheduleEvent(new TrackingScheduleEvent(team.getId()));

        return participant;
    }

    // 참가 인원 조회
    public List<Long> findParticipants(Long postId) {
        return participantRepository.findUserIdsByPostId(postId);
    }

}