package com.example.emergencyassistb4b4.domain.report.repository;

import com.example.emergencyassistb4b4.domain.report.domain.Report;
import com.example.emergencyassistb4b4.domain.report.dto.TodayReportStatusCounts;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long>, ReportRepositoryCustom {

    List<Report> findAllByResponder(User responder);

    @Query(value = """
            SELECT
                r.disaster_type,
                r.status,
                ST_Y(r.location::geometry),
                ST_X(r.location::geometry)
            FROM report r
            WHERE ST_DWithin(
                r.location,
                ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                :radiusMeter
            )
            AND r.created_at >= :fromTime
        """, nativeQuery = true)
    List<Object[]> findNearbyDisasterReportsRaw(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("radiusMeter") int radiusMeter,
        @Param("fromTime") LocalDateTime fromTime
    );

    @Query("""
        select new com.example.emergencyassistb4b4.domain.report.dto.TodayReportStatusCounts(
          coalesce(sum(case when r.status = com.example.emergencyassistb4b4.domain.report.enums.ReportStatus.PENDING
                            and function('date', r.createdAt) = :day then 1 else 0 end), 0),
          coalesce(sum(case when r.status = com.example.emergencyassistb4b4.domain.report.enums.ReportStatus.RECEIVED
                            and function('date', r.createdAt) = :day then 1 else 0 end), 0),
          coalesce(sum(case when r.status = com.example.emergencyassistb4b4.domain.report.enums.ReportStatus.CLOSED
                            and function('date', r.createdAt) = :day then 1 else 0 end), 0)
        )
        from Report r
        where r.responder.id = :responderId
        """)
    TodayReportStatusCounts getTodayReports(
        @Param("responderId") Long responderId,
        @Param("day") java.time.LocalDate day
    );

}