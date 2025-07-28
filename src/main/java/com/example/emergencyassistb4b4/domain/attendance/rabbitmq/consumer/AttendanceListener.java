package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event;

import com.example.emergencyassistb4b4.domain.attendance.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceListener {

    private final RedisService redisService;

    // RabbitMQ 큐에서 메시지를 수신
    @RabbitListener(queues = "attendance-delay-queue")
    public void onRabbitMQStateMessage(Long teamId) {
        try {
            String state = redisService.getRabbitmqState(teamId);
            if ("false".equals(state)) {
                log.info("Attendance state is true for teamId: {}", teamId);
                // 상태가 true일 때 후속 작업 수행
                // 예: 출석 상태 변경, RabbitMQ 메시지 발송 등
            } else {
                log.info("Attendance state is false for teamId: {}", teamId);
                // 상태가 false일 때 처리할 내용
                // 예: 다시 큐에 넣거나 다른 작업 수행
            }
        } catch (Exception e) {
            log.error("Error while processing teamId: {}", teamId, e);
            // 예외 처리: Redis 접근 중 오류가 발생한 경우 로그 남기고 필요시 재시도 로직 추가
        }
    }
}
