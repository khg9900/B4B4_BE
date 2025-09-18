package com.example.emergencyassistb4b4.domain.report.dto;

import com.example.emergencyassistb4b4.domain.report.enums.ReportStatus;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record ReportCursorRequest(
        String province,
        String city,
        ReportStatus status,
        Integer pageSize,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastCreatedAt,
        Long lastId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
) {

    public int effectivePageSize() {

        return (pageSize == null || pageSize <= 0) ? 10 : Math.min(pageSize, 100);
    }
}
