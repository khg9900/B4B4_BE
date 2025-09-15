package com.example.emergencyassistb4b4.domain.volunteer.service;

import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerTeam;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.CheckinPeriodDto;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.CheckinStatusRequest;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.VolunteerParticipationResponse;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.example.emergencyassistb4b4.domain.volunteer.infra.redis.service.TeamParticipationCleanupScheduler;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostStatus;
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
    private final TeamParticipationCleanupScheduler cleanupScheduler;

    @Transactional
    public void joinTeam(Long postId, int teamNumber, User user) {
        LocalDateTime now = LocalDateTime.now();
        Long userId= user.getId();

        // 이미 참여 중인지 체크
        ensureNotAlreadyParticipating(userId, postId);

        // 팀 + post 조회
        VolunteerTeam team = teamRepository.findByPost_IdAndTeamNumber(postId, teamNumber)
                .orElseThrow(() -> new ApiException(ErrorStatus.VOLUNTEER_NOT_FOUND));

        Post post = team.getPost();

        // 체크인 기간 가져오기
        CheckinPeriodDto period = new CheckinPeriodDto(post.getAttendancePolicy().getCheckinStart(),
                post.getAttendancePolicy().getCheckinEnd());

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

        // Redis + DB 저장 처리
        executeWithRetry(
                () -> teamParticipationRedisService.tryJoinTeam(
                        postId, team.getId(), userId, team.getMaxCapacity(), period.checkinEnd()
                ),
                () -> teamParticipationRedisService.cancelJoin(
                        postId, team.getId(), userId, period.checkinEnd()
                )
        );

        // DB 저장
        participantService.joinSave(user, team);

        cleanupScheduler.scheduleCleanup(postId, team.getId(), period.checkinEnd());
    }

    @Transactional
    public void cancelJoin(Long participantId, CheckinStatusRequest request, User user) {
        LocalDateTime now = LocalDateTime.now();
        Long userId= user.getId();

        VolunteerParticipant participant = participantRepository.findByIdAndUserId(participantId, userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.VOLUNTEER_FORBIDDEN));

        VolunteerTeam team = participant.getVolunteerTeam();
        Post post = team.getPost();
        CheckinPeriodDto period = new CheckinPeriodDto(post.getAttendancePolicy().getCheckinStart(),
                post.getAttendancePolicy().getCheckinEnd());

        if (now.isAfter(period.checkinStart())) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }

        executeWithRetry(
                () -> teamParticipationRedisService.cancelJoin(post.getId(), team.getId(), userId, period.checkinEnd()),
                null
        );

        participant.updateStatus(request.getStatus());
    }

    @Transactional(readOnly = true)
    public List<VolunteerParticipationResponse> getMyParticipation(Long userId, CheckinStatus status, LocalDateTime startTime, LocalDateTime endTime) {
        List<VolunteerParticipant> participants = volunteerParticipantRepositoryCustom
                .findAllByUserIdWithPostAndTeam(userId, status, startTime, endTime);
        return participants.stream()
                .map(VolunteerParticipationResponse::from)
                .toList();
    }

    /**
     * 이미 참여 중인지 확인
     */
    private void ensureNotAlreadyParticipating(Long userId, Long postId) {
        if (participantRepository.existsActiveParticipation(userId, postId)) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
        }
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
