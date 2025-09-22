package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AttendanceStateSetEvent {

    private final Long teamId;
    private final LocalDateTime joinedAt;

    // 출석 이벤트 생성
    public AttendanceStateSetEvent(Long teamId, LocalDateTime joinedAt) {
        this.teamId = teamId;
        this.joinedAt = joinedAt;
    }

}
