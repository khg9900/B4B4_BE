package com.example.emergencyassistb4b4.domain.volunteer.dto.Post;

import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostAttendancePolicyDto;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostLocationDto;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostCategory;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDetailResponse {

    private String title;
    private String content;
    private LocalDate volunteerDate;
    private LocalTime volunteerStartTime;
    private LocalTime volunteerEndTime;
    private LocalDate recruitmentStartDate;
    private LocalDate recruitmentEndDate;
    private int totalCapacity;
    private PostCategory category;
    private PostStatus status;

    private PostLocationDto location;
    private PostAttendancePolicyDto attendancePolicy;

    public static PostDetailResponse from(Post post) {
        var location = post.getLocation();
        var policy = post.getAttendancePolicy();

        return PostDetailResponse.builder()
            .title(post.getTitle())
            .content(post.getContent())
            .volunteerDate(post.getVolunteerDate())
            .volunteerStartTime(post.getVolunteerStartTime())
            .volunteerEndTime(post.getVolunteerEndTime())
            .recruitmentStartDate(post.getRecruitmentStartDate())
            .recruitmentEndDate(post.getRecruitmentEndDate())
            .totalCapacity(post.getTotalCapacity())
            .category(post.getCategory())
            .status(post.getStatus())
            .location(PostLocationDto.builder()
                .province(location.getProvince())
                .city(location.getCity())
                .placeName(location.getPlaceName())
                .latitude(location.getLocationLat())
                .longitude(location.getLocationLng())
                .build())
            .attendancePolicy(PostAttendancePolicyDto.builder()
                .checkinStart(policy.getCheckinStart())
                .checkinEnd(policy.getCheckinEnd())
                .allowedRadiusM(policy.getAttendanceRadiusMeters())
                .build())
            .build();
    }
}
