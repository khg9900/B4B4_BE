package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto;

import com.example.emergencyassistb4b4.domain.volunteer.domain.AttendancePolicy;
import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
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

    private Long postId;

    private Long teamId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private double targetLat;

    private double targetLng;

    private int meter;

    private List<Long> participantUserIds;

    // 도메인 객체로부터 TrackingSessionDto 생성
    public static TrackingSessionDto from(
            Post post,
            VolunteerTeam team,
            VolunteerLocation location,
            AttendancePolicy policy,
            List<Long> participantUserIds
    ) {
        return TrackingSessionDto.builder()
                .postId(post.getId())
                .teamId(team.getId())
                .participantUserIds(participantUserIds)
                .targetLat(location.getLocationLat())
                .targetLng(location.getLocationLng())
                .startTime(policy.getCheckinStart())
                .endTime(policy.getCheckinEnd())
                .meter(policy.getAttendanceRadiusMeters())
                .build();
    }
}
