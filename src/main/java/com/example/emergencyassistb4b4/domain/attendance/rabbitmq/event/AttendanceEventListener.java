package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto.RabbitMQ;
import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.service.TrackingService;
import com.example.emergencyassistb4b4.domain.attendance.redis.RabbitMQRedisService;

import io.lettuce.core.RedisException;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceEventListener {

    private final RabbitMQRedisService rabbitMQRedisService;
    private final TrackingService trackingService;

    // 출석 예약 상태 저장
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void onAttendanceStateSet(AttendanceStateSetEvent event) {
        try {
            rabbitMQRedisService.scheduleTrackingStart(event.getTeamId(), event.getJoinedAt());
        } catch (Exception e) {
            log.error("예약 등록 실패 - teamId: {}", event.getTeamId(), e);
        }
    }

    // 출석 시작 처리
    public void onAttendanceStateChanged(Long teamId) {
        try {
            RabbitMQ state = rabbitMQRedisService.getTrackingState(teamId);

            if (shouldStart(state.getJoinedAt()) && !state.isState()) {
                trackingService.scheduleTrackingForTeam(teamId);
                rabbitMQRedisService.updateTrackingState(teamId, state.getJoinedAt());
            }
        } catch (RedisException e) {
            log.error("출석 시작 실패 - Redis 문제, teamId: {}", teamId, e);
        } catch (Exception e) {
            log.error("출석 시작 실패 - 알 수 없는 오류, teamId: {}", teamId, e);
        }
    }

    // 출석 종료 처리
    public void onAttendanceEnded(Long teamId) {
        try {
            RabbitMQ state = rabbitMQRedisService.getTrackingState(teamId);

            if (state.isState()) {
                clearTrackingState(teamId, false);
            } else if (shouldEnd(state.getJoinedAt())) {
                clearTrackingState(teamId, true);
            }
        } catch (RedisException e) {
            log.error("출석 종료 실패 - Redis 문제, teamId: {}", teamId, e);
        } catch (Exception e) {
            log.error("출석 종료 실패 - 알 수 없는 오류, teamId: {}", teamId, e);
        }
    }

    // tracking 상태 삭제
    private void clearTrackingState(Long teamId, boolean warn) {
        rabbitMQRedisService.clearTrackingState(teamId);
        if (warn) {
            log.warn("출석 종료 비정상 - 시작 안됨, teamId: {}", teamId);
        }
    }

    // 출석 시작 조건 체크
    private boolean shouldStart(LocalDateTime joinedAt) {
        return LocalDateTime.now().isAfter(joinedAt.minusMinutes(5));
    }

    // 출석 종료 조건 체크
    private boolean shouldEnd(LocalDateTime joinedAt) {
        return LocalDateTime.now().isAfter(joinedAt);
    }
}
