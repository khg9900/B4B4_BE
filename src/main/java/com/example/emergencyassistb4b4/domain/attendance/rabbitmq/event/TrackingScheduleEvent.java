package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event;
import lombok.Getter;

@Getter
public class TrackingScheduleEvent {

    private final Long teamId;

    public TrackingScheduleEvent(Long teamId) {
        this.teamId = teamId;
    }

}