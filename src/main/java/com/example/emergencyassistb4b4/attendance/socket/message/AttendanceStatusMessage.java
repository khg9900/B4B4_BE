package com.example.emergencyassistb4b4.attendance.socket.message;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendanceStatusMessage {
    private Long volunteerId;
    private boolean present;

    public AttendanceStatusMessage(Long volunteerId, boolean present) {
        this.volunteerId = volunteerId;
        this.present = present;
    }
}
