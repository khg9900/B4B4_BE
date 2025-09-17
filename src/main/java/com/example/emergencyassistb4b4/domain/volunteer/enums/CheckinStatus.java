package com.example.emergencyassistb4b4.domain.volunteer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CheckinStatus {

    PARTICIPATED("참가중"),
    CANCELLED("취소"),
    BLACKLISTED("블랙리스트"),
    PRESENT("출석"),
    ABSENT("결석");

    private final String displayName;

    public boolean isParticipated() {
        return this != CANCELLED && this != BLACKLISTED;
    }
}