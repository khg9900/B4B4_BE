package com.example.emergencyassistb4b4.report.dto;

import com.example.emergencyassistb4b4.report.enums.ReportStatus;

public record ReportStatusResponseDto(Long reportId, ReportStatus status) {
}
