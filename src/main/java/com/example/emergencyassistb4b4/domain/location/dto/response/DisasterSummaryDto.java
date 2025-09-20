package com.example.emergencyassistb4b4.domain.location.dto.response;

import com.example.emergencyassistb4b4.domain.report.enums.DisasterType;
import com.example.emergencyassistb4b4.domain.report.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DisasterSummaryDto {

    private DisasterType disasterType;

    private ReportStatus status;

    private long count;

    private double latitude;

    private double longitude;

}


