package com.example.emergencyassistb4b4.domain.attendance.redis;


import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event.AttendanceEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQRedisScheduler {

    private final AttendanceEventListener attendanceEventListener;
    private final RabbitMQRedisService rabbitMQRedisService;

    @Scheduled(cron = "0 * * * * *")
    public void ScheduledRun(){
        rabbitMQRedisService.getAllTrackingStates().forEach(attendanceEventListener::onAttendanceStateChanged);


    }

    @Scheduled(cron = "0 */5 * * * *")
    public void ScheduledRunDown(){
        rabbitMQRedisService.getAllTrackingStates().forEach(attendanceEventListener::onAttendanceEnded);

    }


}
