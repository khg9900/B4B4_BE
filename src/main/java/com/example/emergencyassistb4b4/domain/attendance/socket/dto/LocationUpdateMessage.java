package com.example.emergencyassistb4b4.domain.attendance.socket.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LocationUpdateMessage {
    private Long volunteerId;
    private double latitude;
    private double longitude;
}

