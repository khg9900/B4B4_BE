package com.example.emergencyassistb4b4.volunteer.domain;

import com.example.emergencyassistb4b4.global.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AttendancePolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime checkinStart;
    private LocalDateTime checkinEnd;

    private int attendanceRadiusMeters;
    private int minCheckinMinutes;

    @OneToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public void setPost(Post post) {
        this.post = post;
    }

    public void update(LocalDateTime start, LocalDateTime end, int radius, int minutes) {
        this.checkinStart = start;
        this.checkinEnd = end;
        this.attendanceRadiusMeters = radius;
        this.minCheckinMinutes = minutes;
    }
}

