package com.example.emergencyassistb4b4.alert.domain.volunteer;

import com.example.emergencyassistb4b4.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@Table(name = "volunteer_alert")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SequenceGenerator(
    name = "volunteer_alert_seq_gen",
    sequenceName = "volunteer_alert_seq",
    allocationSize = 50
)
public class VolunteerAlert extends BaseEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "volunteer_alert_seq_gen"
    )
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String placeName;

    @Column(nullable = false)
    private LocalDateTime checkinStart;
}
