package com.example.emergencyassistb4b4.domain.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageWrapper {
    private SessionState sessionState;
    private TrackingSessionDto payload;
}
