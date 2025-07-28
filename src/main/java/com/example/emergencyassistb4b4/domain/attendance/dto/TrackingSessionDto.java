<<<<<<<< HEAD:src/main/java/com/example/emergencyassistb4b4/domain/attendance/rabbitmq/dto/TrackingSessionDto.java
package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto;
========
package com.example.emergencyassistb4b4.domain.attendance.dto;
>>>>>>>> 91a6ba41dbd82173278648d8e0dd59d73ebbb3e5:src/main/java/com/example/emergencyassistb4b4/domain/attendance/dto/TrackingSessionDto.java

import com.example.emergencyassistb4b4.domain.volunteer.domain.AttendancePolicy;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerLocation;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerTeam;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackingSessionDto {
    private Long teamId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    private double targetLat;
    private double targetLng;
    private int meter;
    private long intervalSeconds;
    private List<Long> participantUserIds;

    public static TrackingSessionDto from(
            VolunteerTeam team,
            VolunteerLocation location,
            AttendancePolicy policy,
            List<Long> participantUserIds
    ) {
        return TrackingSessionDto.builder()
                .teamId(team.getId())
                .participantUserIds(participantUserIds)
                .targetLat(location.getLocationLat())
                .targetLng(location.getLocationLng())
                .startTime(policy.getCheckinStart())
                .endTime(policy.getCheckinEnd())
                .intervalSeconds(60L)
                .meter(policy.getAttendanceRadiusMeters())
                .build();
    }
}
