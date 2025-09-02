package com.example.emergencyassistb4b4.domain.volunteer.dto.Post;

import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostCategory;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostStatus;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostsResponse {
    private Long id;
    private String title;
    private LocalDate volunteerDate;
    private String province;
    private String city;
    private PostCategory category;
    private int totalCapacity;
    private LocalDate recruitmentStartDate;
    private LocalDate recruitmentEndDate;
    private PostStatus status;

    public static PostsResponse from(Post post) {
        return PostsResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .volunteerDate(post.getVolunteerDate())
                .province(post.getLocation().getProvince())
                .city(post.getLocation().getCity())
                .category(post.getCategory())
                .totalCapacity(post.getTotalCapacity())
                .recruitmentStartDate(post.getRecruitmentStartDate())
                .recruitmentEndDate(post.getRecruitmentEndDate())
                .status(post.getStatus())
                .build();
    }
}
