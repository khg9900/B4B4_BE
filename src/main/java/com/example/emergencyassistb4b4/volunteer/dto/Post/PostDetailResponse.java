package com.example.emergencyassistb4b4.volunteer.dto.Post;

import com.example.emergencyassistb4b4.volunteer.domain.Post;
import com.example.emergencyassistb4b4.volunteer.dto.Post.common.PostAttendancePolicyDto;
import com.example.emergencyassistb4b4.volunteer.dto.Post.common.PostLocationDto;
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
    private String category;
    private int totalCapacity;
    private int teamSize;

    private PostLocationDto location;
    private PostAttendancePolicyDto attendancePolicy;

    public static PostDetailResponse from(Post post) {
        return PostDetailResponse.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory().name())
                .totalCapacity(post.getTotalCapacity())
                .teamSize(post.getTeamSize())
                .location(PostLocationDto.builder()
                        .placeName(post.getLocation().getPlaceName())
                        .latitude(post.getLocation().getLocationLat())
                        .longitude(post.getLocation().getLocationLng())
                        .build())
                .attendancePolicy(PostAttendancePolicyDto.builder()
                        .checkinStart(post.getAttendancePolicy().getCheckinStart())
                        .checkinEnd(post.getAttendancePolicy().getCheckinEnd())
                        .allowedRadiusM(post.getAttendancePolicy().getAttendanceRadiusMeters())
                        .minStayMinutes(post.getAttendancePolicy().getMinCheckinMinutes())
                        .build())
                .build();
    }
}
