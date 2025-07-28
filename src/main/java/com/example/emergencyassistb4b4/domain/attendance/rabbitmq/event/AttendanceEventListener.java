package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event;

import com.example.emergencyassistb4b4.domain.attendance.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceEventListener {

    private final RedisService redisService;
    private final RabbitTemplate rabbitTemplate;

    // 출석 상태 설정 후 커밋 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void onAttendanceStateSet(Long teamId) {
        // 트랜잭션 커밋 후 처리되도록 보장
        try {
            // 출석 상태를 Redis에 설정 (트랜잭션 커밋 후)
            redisService.setRabbitMQState(teamId);
            log.info("Attendance state set to false for teamId: {}", teamId);
        } catch (Exception e) {
            log.error("Error setting RabbitMQ state for teamId: {}", teamId, e);
        }
    }

    // 출석 상태 변경 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void onAttendanceStateChanged(Long teamId) {
        // 트랜잭션 커밋 후 처리되도록 보장
        try {
            String state = redisService.getRabbitmqState(teamId);

            // 출석 상태가 false일 때만 처리
            if ("false".equals(state)) {
                // 출석 시작 메시지 발행
                rabbitTemplate.convertAndSend("attendance.exchange", "attendance.start", teamId.toString());
                // 상태를 true로 변경
                redisService.changeRabbitMQState(teamId);
                log.info("Attendance state set to true for teamId: {}", teamId);
            }
        } catch (Exception e) {
            log.error("Error processing attendance state change for teamId: {}", teamId, e);
        }
    }

    // 출석 종료 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void onAttendanceEnded(Long teamId) {
        // 트랜잭션 커밋 후 처리되도록 보장
        try {
            String state = redisService.getRabbitmqState(teamId);

            // 상태가 false가 아니면 상태 삭제
            if (state != null && !"false".equals(state)) {
                redisService.deleteRabbitMQState(teamId);
                log.info("Attendance state deleted for teamId: {}", teamId);
            } else {
                log.warn("No valid attendance state to delete for teamId: {}", teamId);
            }
        } catch (Exception e) {
            log.error("Error ending attendance for teamId: {}", teamId, e);
        }
    }
}
