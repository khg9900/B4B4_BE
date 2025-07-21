package com.example.emergencyassistb4b4.domain.attendance.event;

import com.example.emergencyassistb4b4.domain.attendance.service.TrackingService;
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
