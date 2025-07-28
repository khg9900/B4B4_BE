<<<<<<<< HEAD:src/main/java/com/example/emergencyassistb4b4/domain/attendance/rabbitmq/dto/IndividualTrackingSessionDto.java
package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto;
========
package com.example.emergencyassistb4b4.domain.attendance.dto;
>>>>>>>> 91a6ba41dbd82173278648d8e0dd59d73ebbb3e5:src/main/java/com/example/emergencyassistb4b4/domain/attendance/dto/IndividualTrackingSessionDto.java

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndividualTrackingSessionDto {

    private Long teamId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private double targetLat;
    private double targetLng;

    private int meter;
    private long intervalSeconds;

    private Long participantUserId;
}
