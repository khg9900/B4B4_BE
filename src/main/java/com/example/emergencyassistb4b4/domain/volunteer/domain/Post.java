package com.example.emergencyassistb4b4.domain.volunteer.domain;

import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.UpdatePostRequest;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostAttendancePolicyDto;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostLocationDto;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostStatus;
import com.example.emergencyassistb4b4.global.entity.BaseEntity;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostCategory;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;


import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post")
@SequenceGenerator(
    name = "post_seq_gen",
    sequenceName = "post_seq",
    allocationSize = 50
)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "post_seq_gen"
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    private String title;

    @Column(columnDefinition = "text")
    private String content;

    private LocalDate volunteerDate;

    private LocalTime volunteerStartTime;

    private LocalTime volunteerEndTime;

    private LocalDate recruitmentStartDate;

    private LocalDate recruitmentEndDate;

    private int totalCapacity;

    private int teamSize;

    @Enumerated(EnumType.STRING)
    private PostCategory category;

    @Setter
    @Enumerated(EnumType.STRING)
    private PostStatus status;

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

    public boolean isOpen() {
        return PostStatus.OPEN.equals(this.status);
    }

    public boolean isNotOpen() {
        return !isOpen();
    }

    public void update(UpdatePostRequest request) {

        this.title = request.getTitle();
        this.content = request.getContent();
        this.volunteerDate = request.getVolunteerDate();
        this.volunteerStartTime= request.getVolunteerStartTime();
        this.volunteerEndTime= request.getVolunteerEndTime();
        this.recruitmentStartDate = request.getRecruitmentStartDate();
        this.recruitmentEndDate = request.getRecruitmentEndDate();
        this.status = request.getStatus();

        PostLocationDto location = request.getLocation();
        this.getLocation().update(
            location.getProvince(),
            location.getCity(),
            location.getPlaceName(),
            location.getLatitude(),
            location.getLongitude()
        );

        PostAttendancePolicyDto policy = request.getAttendancePolicy();
        this.getAttendancePolicy().update(
            policy.getCheckinStart(),
            policy.getCheckinEnd(),
            policy.getAllowedRadiusM()
        );
    }

    public void addTeams(List<VolunteerTeam> teamList) {
        this.teams = teamList;
        for (VolunteerTeam team : teamList) {
            team.setPost(this);
        }
    }
}