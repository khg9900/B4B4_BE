package com.example.emergencyassistb4b4.domain.volunteer.service;

import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerTeam;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.CheckinPeriodDto;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.CheckinStatusRequest;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.VolunteerParticipationResponse;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service.TeamParticipationRedisService;
import com.example.emergencyassistb4b4.domain.volunteer.repository.PostRepository;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepositoryCustom;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerTeamRepository;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VolunteerJoinService {

    private final VolunteerTeamRepository teamRepository;
    private final VolunteerParticipantRepository participantRepository;
    private final PostRepository postRepository;
    private final TeamParticipationRedisService teamParticipationRedisService;
    private final VolunteerParticipantService participantService;
    private final VolunteerParticipantRepositoryCustom volunteerParticipantRepositoryCustom;

    @Transactional
    public void joinTeam(Long postId, int teamNumber, Long userId) {
        LocalDateTime now = LocalDateTime.now();

        // 이미 활동 중인지 확인
        if (participantRepository.existsActiveParticipation(userId,postId)) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }

        // 체크인 기간 조회
        CheckinPeriodDto period = postRepository.findCheckinPeriodByPostId(postId)
                .orElseThrow(() -> new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST));

        if (now.isAfter(period.checkinStart().minusMinutes(5))) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }

        // 다른 활동과 겹치는지 확인
        boolean isOverlapping = postRepository.existsOverlappingCheckinPeriod(
                userId, postId, period.checkinStart(), period.checkinEnd()
        );
        if (isOverlapping) {
            throw new ApiException(ErrorStatus.VOLUNTEER_CONFLICT);
        }

        // 팀 조회
        VolunteerTeam team = teamRepository.findByPost_IdAndTeamNumber(postId, teamNumber)
                .orElseThrow(() -> new ApiException(ErrorStatus.VOLUNTEER_NOT_FOUND));

        // Redis + DB 저장 처리
        executeWithRetry(
                () -> teamParticipationRedisService.tryJoinTeam(
                        postId, team.getId(), userId, team.getMaxCapacity(), period.checkinEnd()
                ),
                () -> {
                    // DB 저장 실패 시 Redis 롤백
                    try {
                        teamParticipationRedisService.cancelJoin(postId, team.getId(), userId);
                    } catch (Exception ex) {
                        log.error("Redis 롤백 실패 postId={}, teamId={}, userId={}", postId, team.getId(), userId, ex);
                    }
                }
        );

        // DB 저장
        participantService.joinSave(userId, team.getId());
    }

    @Transactional
    public void cancelJoin(Long participantId, CheckinStatusRequest request, Long userId) {
        LocalDateTime now = LocalDateTime.now();

        VolunteerParticipant participant = participantRepository.findByIdAndUserId(participantId, userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.VOLUNTEER_FORBIDDEN));

        Long teamId = participant.getVolunteerTeam().getId();
        Long postId = participant.getVolunteerTeam().getPost().getId();

        CheckinPeriodDto period = postRepository.findCheckinPeriodByPostId(postId)
                .orElseThrow(() -> new ApiException(ErrorStatus.VOLUNTEER_NOT_FOUND));

        if (now.isAfter(period.checkinStart())) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }

        executeWithRetry(
                () -> teamParticipationRedisService.cancelJoin(postId, teamId, userId),
                null // 취소는 DB 롤백 필요 없음
        );

        participant.updateStatus(request.getStatus());
    }

    @Transactional(readOnly = true)
    public List<VolunteerParticipationResponse> getMyParticipation(Long userId, CheckinStatus status,LocalDateTime startTime, LocalDateTime endTime) {
        List<VolunteerParticipant> participants = volunteerParticipantRepositoryCustom.findAllByUserIdWithPostAndTeam(userId,status,startTime,endTime);
        return participants.stream()
                .map(VolunteerParticipationResponse::from)
                .toList();
    }

    /**
     * Redis 작업 재시도 + 필요 시 롤백 처리
     */
    private void executeWithRetry(Runnable action, Runnable rollbackAction) {
        int maxAttempts = 5;
        int attempt = 0;

        while (attempt < maxAttempts) {
            try {
                action.run();
                return; // 성공하면 종료
            } catch (Exception e) {
                log.error("Redis 작업 실패, 재시도 중: {}", attempt, e);
                attempt++;
                if (attempt >= maxAttempts) {
                    // 재시도 최대치 도달
                    if (rollbackAction != null) {
                        try {
                            rollbackAction.run();
                        } catch (Exception ex) {
                            log.error("Redis 롤백 실패", ex);
                        }
                    }
                    throw new ApiException(ErrorStatus.REDIS_SERVER_ERROR);
                }
                try {
                    Thread.sleep((long) Math.pow(2, attempt) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
