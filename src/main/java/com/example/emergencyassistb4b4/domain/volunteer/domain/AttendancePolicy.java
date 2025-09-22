package com.example.emergencyassistb4b4.domain.volunteer.domain;

import com.example.emergencyassistb4b4.global.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "attendance_policy")
@SequenceGenerator(
    name = "attendance_policy_seq_gen",
    sequenceName = "attendance_policy_seq",
    allocationSize = 50
)
public class AttendancePolicy extends BaseEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "attendance_policy_seq_gen"
    )
    private Long id;

    private LocalDateTime checkinStart;
    private LocalDateTime checkinEnd;

    private int attendanceRadiusMeters;

    @Setter
    @OneToOne
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private Post post;

    public void update(LocalDateTime start, LocalDateTime end, int radius) {
        this.checkinStart = start;
        this.checkinEnd = end;
        this.attendanceRadiusMeters = radius;
    }
}

