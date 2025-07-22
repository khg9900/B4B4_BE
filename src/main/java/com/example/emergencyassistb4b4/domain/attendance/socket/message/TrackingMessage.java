package com.example.emergencyassistb4b4.domain.attendance.socket.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
public class TrackingMessage {
    private final String type;
    private final String content;
}