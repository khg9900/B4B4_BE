package com.example.emergencyassistb4b4.domain.alert.domain.report;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@Table(
    name = "user_report_alert",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "alert_id"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SequenceGenerator(
    name = "user_report_alert_seq_gen",
    sequenceName = "user_report_alert_seq",
    allocationSize = 50
)
public class UserReportAlert {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "user_report_alert_seq_gen"
    )
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private ReportAlert reportAlert;

    public static List<UserReportAlert> fromUsers(ReportAlert alert, List<Long> userIds) {
        return userIds.stream()
            .map(userId -> UserReportAlert.builder()
                .userId(userId)
                .reportAlert(alert)
                .build())
            .toList();
    }
}
