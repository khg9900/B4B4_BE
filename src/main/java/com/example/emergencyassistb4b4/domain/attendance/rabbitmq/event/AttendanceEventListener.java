package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.RabbitMQ;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.service.TrackingService;
import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;
import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceEventListener {

    private final RabbitMQRedisService rabbitMQRedisService;
    private final TrackingService trackingService;

    // 1. 출석 예약 상태 저장
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void onAttendanceStateSet(AttendanceStateSetEvent event) {
        try {
            rabbitMQRedisService.scheduleTrackingStart(event.getTeamId(), event.getJoinedAt());
        } catch (Exception e) {
            log.error("[예약 등록 실패] teamId: {}", event.getTeamId(), e);
        }
    }

    // 2. 출석 시작 처리
    public void onAttendanceStateChanged(Long teamId) {
        try {
            RabbitMQ state = rabbitMQRedisService.getTrackingState(teamId);

            if (shouldStart(state.getJoinedAt()) && !state.isState()) {
                trackingService.scheduleTrackingForTeam(teamId);
                rabbitMQRedisService.updateTrackingState(teamId, state.getJoinedAt()); // state = true
                log.info("[출석 시작] 출석 상태 변경 완료 - teamId: {}", teamId);
            } else {
                log.debug("[조건 미충족] 출석 시작 안함 - teamId: {}, shouldStart: {}, isNotStarted: {}",
                        teamId, shouldStart(state.getJoinedAt()), !state.isState());
            }
        } catch (RedisException e) {
            log.error("[출석 시작 실패 - Redis 문제] teamId: {}", teamId, e);
        } catch (Exception e) {
            log.error("[출석 시작 실패 - 알 수 없는 오류] teamId: {}", teamId, e);
        }
    }

    // 3. 출석 종료 처리
    public void onAttendanceEnded(Long teamId) {
        try {
            RabbitMQ state = rabbitMQRedisService.getTrackingState(teamId);

            if (state.isState()) {
                clearTrackingStateWithLog(teamId, "[출석 종료] 출석 상태 삭제 완료", false);
            } else if (shouldEnd(state.getJoinedAt())) {
                clearTrackingStateWithLog(teamId, "[출석 종료 비정상] 출석 시작 안됨", true);
            } else {
                log.debug("[출석 종료 스킵] 아직 출석 시간 전 - teamId: {}, joinedAt: {}", teamId, state.getJoinedAt());
            }

        } catch (RedisException e) {
            log.error("[출석 종료 실패 - Redis 문제] teamId: {}", teamId, e);
        } catch (Exception e) {
            log.error("[출석 종료 실패 - 알 수 없는 오류] teamId: {}", teamId, e);
        }
    }

    // ---------------- 유틸 메서드 ----------------

    private void clearTrackingStateWithLog(Long teamId, String message, boolean warn) {
        rabbitMQRedisService.clearTrackingState(teamId);
        if (warn) log.warn(message, teamId);
    }

    private boolean shouldStart(LocalDateTime joinedAt) {
        return LocalDateTime.now().isAfter(joinedAt.minusMinutes(5));
    }

    private boolean shouldEnd(LocalDateTime joinedAt) {
        return LocalDateTime.now().isAfter(joinedAt);
    }
}
