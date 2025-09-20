package com.example.emergencyassistb4b4.domain.attendance.socket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageWrapper {

    private String type;

    private LocationUpdateMessage data;
}