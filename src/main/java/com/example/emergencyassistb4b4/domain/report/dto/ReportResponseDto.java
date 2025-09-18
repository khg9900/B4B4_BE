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
public class ReportResponseDto {

    private final Long id;

    private final Long reporter;

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

    public static ReportResponseDto from(Report report) {

        return ReportResponseDto.builder()
                .id(report.getId())
                .reporter(report.getReporter().getId())
                .disasterType(report.getDisasterType())
                .description(report.getDescription())
                .imageUrl(report.getImageUrl())
                .videoUrl(report.getVideoUrl())
                .status(report.getStatus())
                .province(report.getProvince())
                .city(report.getCity())
                .locationLat(report.getLocation().getY())
                .locationLng(report.getLocation().getX())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
