package com.example.emergencyassistb4b4.domain.attendance.socket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WebSocketMessageWrapper {
    private String type;
    private LocationUpdateMessage data;

    public void setType(String type) {
        this.type = type;
    }

    public void setData(LocationUpdateMessage data) {
        this.data = data;
    }
}