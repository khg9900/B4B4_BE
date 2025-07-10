package com.example.emergencyassistb4b4.report.repository;

import com.example.emergencyassistb4b4.location.dto.response.DisasterReportSimpleDto;
import com.example.emergencyassistb4b4.report.domain.Report;
import com.example.emergencyassistb4b4.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long>,ReportRepositoryCustom {

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

}