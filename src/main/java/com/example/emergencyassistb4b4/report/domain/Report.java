package com.example.emergencyassistb4b4.report.domain;

import com.example.emergencyassistb4b4.report.enums.DisasterType;
import com.example.emergencyassistb4b4.global.entity.BaseEntity;
import com.example.emergencyassistb4b4.report.enums.ReportStatus;
import com.example.emergencyassistb4b4.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Getter
@Builder
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "report")
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_seq_gen")
    @SequenceGenerator(
            name = "report_seq_gen",
            sequenceName = "report_seq", // DB에 시퀀스 직접 생성 필요
            allocationSize = 50
    )
    private Long id;

    // 신고자 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    // 재난 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "disaster_type", nullable = false)
    private DisasterType disasterType;

    // 설명
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    // 이미지 URL
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    // 비디오 URL
    @Column(name = "video_url", length = 255)
    private String videoUrl;

    // 상태
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.PENDING;  // 생성 시 기본값

    // 행정구역 (시/도)
    @Column(name = "province", nullable = false, length = 255)
    private String province;

    // 행정구역 (구/군)
    @Column(name = "city", length = 255)
    private String city;

    // 위치 정보 (PostGIS Point 타입)
    @Column(columnDefinition = "geography(Point, 4326)")
    private Point location;

    // 공공기관 (신고 수신자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responder_id")
    private User responder;

    // 상태 변경 메서드
    public void updateStatus(ReportStatus newStatus) {
        this.status = newStatus;
    }
}
