package com.example.emergencyassistb4b4.domain.report.dto;

import com.example.emergencyassistb4b4.domain.report.enums.DisasterType;
import com.example.emergencyassistb4b4.domain.report.domain.Report;
import com.example.emergencyassistb4b4.domain.report.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReportDto {

    private final Long id;

    private final Long reporterId;

    private final DisasterType disasterType;

    private final String description;

    private final String imageUrl;

    private final String videoUrl;

    private final ReportStatus status;

    private final String province;

    private final String city;

    private final Double locationLat;

    private final Double locationLng;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public static ReportDto of(Report r) {

        return ReportDto.builder()
                .id(r.getId())
                .reporterId(r.getReporter().getId())
                .disasterType(r.getDisasterType())
                .description(r.getDescription())
                .imageUrl(r.getImageUrl())
                .videoUrl(r.getVideoUrl())
                .status(r.getStatus())
                .province(r.getProvince())
                .city(r.getCity())
                .locationLat(r.getLocation().getY())
                .locationLng(r.getLocation().getX())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
