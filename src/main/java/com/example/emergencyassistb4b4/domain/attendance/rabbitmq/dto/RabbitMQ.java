package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RabbitMQ {
    boolean state;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime joinedAt;

    public void updateState(boolean state) {
        this.state = state;
    }

    public void updateJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public boolean isScheduledTimeValid() {
        return joinedAt != null && joinedAt.isAfter(LocalDateTime.now());
    }

}
