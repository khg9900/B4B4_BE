package com.example.emergencyassistb4b4.domain.report.repository;

import com.example.emergencyassistb4b4.domain.report.domain.Report;
import com.example.emergencyassistb4b4.domain.report.enums.ReportStatus;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepositoryCustom {

    // 공공기관: 주변 신고 목록 조회 (거리순 + 최신순 페이징)
    Slice<Report> findNearby(String province, String city, ReportStatus status, Pageable pageable);

    List<Report> findByReporterByCursor(Long userId, ReportStatus status,
                                        LocalDate start, LocalDate end,
                                        LocalDateTime lastCreatedAt, Long lastId,
                                        int limitPlusOne, boolean desc);

}

