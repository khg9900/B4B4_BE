package com.example.emergencyassistb4b4.volunteer.domain;

import com.example.emergencyassistb4b4.global.entity.BaseEntity;
import com.example.emergencyassistb4b4.user.domain.User;
import com.example.emergencyassistb4b4.volunteer.enums.PostCategory;
import jakarta.persistence.*;
import lombok.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@AllArgsConstructor
@Table(name = "Post")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Enumerated(EnumType.STRING)
    private PostCategory category;

    private String title;

    @Column(columnDefinition = "text")
    private String content;

    private int totalCapacity;
    private int teamSize;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VolunteerTeam> teams = new ArrayList<>();

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private VolunteerLocation location;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private AttendancePolicy attendancePolicy;

    public void setLocation(VolunteerLocation location) {
        this.location = location;
        location.setPost(this);
    }

    public void setAttendancePolicy(AttendancePolicy policy) {
        this.attendancePolicy = policy;
        policy.setPost(this);
    }

    public void updateLocation(String placeName, Double latitude, Double longitude) {
        if (this.location == null) {
            this.location = new VolunteerLocation();
        }
        this.location.update(placeName, latitude, longitude);
    }

    public void updateAttendancePolicy(LocalDateTime start, LocalDateTime end, int radius, int minutes) {
        if (this.attendancePolicy == null) {
            this.attendancePolicy = new AttendancePolicy();
        }
        this.attendancePolicy.update(start, end, radius, minutes);
    }

    public void addTeams(List<VolunteerTeam> teamList) {
        this.teams = teamList;
        for (VolunteerTeam team : teamList) {
            team.setPost(this);
        }
    }
}