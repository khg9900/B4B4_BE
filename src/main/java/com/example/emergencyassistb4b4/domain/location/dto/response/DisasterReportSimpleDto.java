package com.example.emergencyassistb4b4.domain.location.dto.response;

import com.example.emergencyassistb4b4.domain.report.enums.DisasterType;
import com.example.emergencyassistb4b4.domain.report.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DisasterReportSimpleDto {
    private DisasterType disasterType;
    private ReportStatus status;
    private double latitude;
    private double longitude;

}
