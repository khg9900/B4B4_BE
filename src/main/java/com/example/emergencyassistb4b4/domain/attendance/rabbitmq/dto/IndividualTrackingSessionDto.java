package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto;

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

    private Long postId;

    private Long teamId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private double targetLat;

    private double targetLng;

    private int meter;

    private Long participantUserId;

    // TrackingSessionDto에서 개인별 세션 DTO를 생성
    public static IndividualTrackingSessionDto buildIndividualDto(TrackingSessionDto dto, Long volunteerId) {
        return IndividualTrackingSessionDto.builder()
                .postId(dto.getPostId())
                .teamId(dto.getTeamId())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .targetLat(dto.getTargetLat())
                .targetLng(dto.getTargetLng())
                .meter(dto.getMeter())
                .participantUserId(volunteerId)
                .build();
    }


}
