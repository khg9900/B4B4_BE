package com.example.emergencyassistb4b4.report.repository;


import com.example.emergencyassistb4b4.report.domain.Report;
import com.example.emergencyassistb4b4.report.enums.ReportStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;

public interface ReportRepositoryCustom {
    /**
     * 주변 신고 목록 조회 공공기관용 (거리순 + 최신순 페이징)
     * @param si        시(지역)
     * @param gu        구(지역)
     * @param status    상태
     * @param pageable  페이징
     * @return          지정된 반경과 상태 조건을 만족하는 신고들을 DTO로 매핑하여 페이징(Slice)로 반환
     */
        Slice<Report> findNearby(String si, String gu, ReportStatus status, Pageable pageable);

    /**
     * 작성자(userId)가 신고한 목록 조회 (상태 + 기간 페이징, 마이페이지용)
     * @param userId    작성자 Id
     * @param status    상태
     * @param start     시작일시
     * @param end       종료일시
     * @param pageable  페이징 정보
     * @return          해당 작성자가 지정된 상태와 기간 조건으로 생성한 신고들을 DTO로 매핑하여 페이징(Slice) 형태로 반환
     */
        Slice<Report> findByReporter(Long userId, ReportStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable);
    }

