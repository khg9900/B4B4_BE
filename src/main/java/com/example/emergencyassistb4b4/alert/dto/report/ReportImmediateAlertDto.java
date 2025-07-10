package com.example.emergencyassistb4b4.alert.dto.report;

import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportImmediateAlertDto {

    private Long governmentId;
    private String disasterType;
    private String description;
    private String province;
    private String city;
    private LocalDateTime reportedAt;

    public static ReportImmediateAlertDto fromEvent(DisasterReportedEvent event) {

        return ReportImmediateAlertDto.builder()
            .governmentId(event.getGovernmentId())
            .disasterType(event.getDisasterType())
            .description(event.getDescription())
            .province(event.getProvince())
            .city(event.getCity())
            .reportedAt(event.getReportedAt())
            .build();
    }
}
