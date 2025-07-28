<<<<<<<< HEAD:src/main/java/com/example/emergencyassistb4b4/domain/attendance/rabbitmq/event/TrackingScheduleEventListener.java
package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event;

import com.example.emergencyassistb4b4.domain.attendance.rabbitmq.service.TrackingService;
========
package com.example.emergencyassistb4b4.domain.attendance.event;

import com.example.emergencyassistb4b4.domain.attendance.service.TrackingService;
>>>>>>>> 91a6ba41dbd82173278648d8e0dd59d73ebbb3e5:src/main/java/com/example/emergencyassistb4b4/domain/attendance/event/TrackingScheduleEventListener.java
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TrackingScheduleEventListener {

    private final TrackingService trackingService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTrackingScheduleEvent(TrackingScheduleEvent event) {
        trackingService.scheduleTrackingForTeam(event.getTeamId());
    }

}
