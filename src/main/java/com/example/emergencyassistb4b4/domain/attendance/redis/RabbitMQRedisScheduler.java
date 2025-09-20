package com.example.emergencyassistb4b4.domain.attendance.redis;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event.AttendanceEventListener;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RabbitMQRedisScheduler {

    private final AttendanceEventListener attendanceEventListener;
    private final RabbitMQRedisService rabbitMQRedisService;

    @Scheduled(cron = "0 * * * * *")
    // 모든 진행 중 트래킹 상태에 대해 출석 상태 변경 이벤트 처리
    public void runAttendanceStateCheck() {
        rabbitMQRedisService.getAllTrackingStates()
                .forEach(attendanceEventListener::onAttendanceStateChanged);
    }

    @Scheduled(cron = "0 */5 * * * *")
    // 모든 진행 중 트래킹 상태에 대해 출석 종료 이벤트 처리
    public void runAttendanceEndCheck() {
        rabbitMQRedisService.getAllTrackingStates()
                .forEach(attendanceEventListener::onAttendanceEnded);
    }
}
