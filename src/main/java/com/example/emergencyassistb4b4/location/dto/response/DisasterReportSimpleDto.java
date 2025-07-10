package com.example.emergencyassistb4b4.location.dto.response;

import com.example.emergencyassistb4b4.report.enums.DisasterType;
import com.example.emergencyassistb4b4.report.enums.ReportStatus;
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
