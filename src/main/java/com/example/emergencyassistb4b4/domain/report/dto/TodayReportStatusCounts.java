package com.example.emergencyassistb4b4.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TodayReportStatusCounts {

    private long pending;

    private long received;

    private long closed;

}
