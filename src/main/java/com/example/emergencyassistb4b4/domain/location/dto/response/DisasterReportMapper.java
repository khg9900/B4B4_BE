package com.example.emergencyassistb4b4.domain.location.dto.response;

import com.example.emergencyassistb4b4.domain.report.enums.DisasterType;
import com.example.emergencyassistb4b4.domain.report.enums.ReportStatus;

import java.util.List;

public class DisasterReportMapper {

    public static List<DisasterReportSimpleDto> map(List<Object[]> rawResults) {
        return rawResults.stream().map(row -> {
            String disasterTypeStr = (String) row[0];
            String statusStr = (String) row[1];
            double lat = ((Number) row[2]).doubleValue();
            double lng = ((Number) row[3]).doubleValue();

            return new DisasterReportSimpleDto(
                    DisasterType.valueOf(disasterTypeStr),
                    ReportStatus.valueOf(statusStr),
                    lat,
                    lng
            );
        }).toList();
    }
}