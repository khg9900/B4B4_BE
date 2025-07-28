<<<<<<<< HEAD:src/main/java/com/example/emergencyassistb4b4/domain/attendance/rabbitmq/event/TrackingScheduleEvent.java
package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event;
========
package com.example.emergencyassistb4b4.domain.attendance.event;
>>>>>>>> 91a6ba41dbd82173278648d8e0dd59d73ebbb3e5:src/main/java/com/example/emergencyassistb4b4/domain/attendance/event/TrackingScheduleEvent.java

import lombok.Getter;

@Getter
public class TrackingScheduleEvent {

    private final Long teamId;

    public TrackingScheduleEvent(Long teamId) {
        this.teamId = teamId;
    }

}