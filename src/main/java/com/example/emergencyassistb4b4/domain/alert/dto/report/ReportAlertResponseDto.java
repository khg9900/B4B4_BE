package com.example.emergencyassistb4b4.domain.alert.dto.report;

import com.example.emergencyassistb4b4.domain.alert.domain.report.ReportAlert;
import com.example.emergencyassistb4b4.domain.alert.domain.report.UserReportAlert;
import com.example.emergencyassistb4b4.domain.alert.dto.response.UserAlert;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReportAlertResponseDto implements UserAlert {

    private Long id;
    private String province;
    private String city;
    private String disasterType;
    private Long count;
    private LocalDateTime createdAt;

    public static ReportAlertResponseDto fromUserReportAlert(UserReportAlert userReportAlert) {
        ReportAlert reportAlert = userReportAlert.getReportAlert();

        return ReportAlertResponseDto.builder()
            .id(userReportAlert.getId())
            .province(reportAlert.getProvince())
            .city(reportAlert.getCity())
            .disasterType(reportAlert.getDisasterType())
            .count(reportAlert.getCount())
            .createdAt(reportAlert.getCreatedAt())
            .build();
    }
}
