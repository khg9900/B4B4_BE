package com.example.emergencyassistb4b4.domain.attendance.socket.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStatusMessage {

    private Long volunteerId;

    private boolean present;
}
