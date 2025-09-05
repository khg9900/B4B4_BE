package com.example.emergencyassistb4b4.domain.volunteer.dto.Post;

import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostCategory;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostTotalResponse {
    private Long id;
    private String title;
    private LocalDate volunteerDate;
    private String province;
    private String city;
    private PostCategory category;
    private int totalCapacity;
    private int currentParticipants;
    private LocalDate recruitmentStartDate;
    private LocalDate recruitmentEndDate;
    private PostStatus status;

    public static PostTotalResponse from(Post post, int currentParticipants) {
        return PostTotalResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .volunteerDate(post.getVolunteerDate())
                .province(post.getLocation().getProvince())
                .city(post.getLocation().getCity())
                .category(post.getCategory())
                .totalCapacity(post.getTotalCapacity())
                .currentParticipants(currentParticipants)
                .recruitmentStartDate(post.getRecruitmentStartDate())
                .recruitmentEndDate(post.getRecruitmentEndDate())
                .status(post.getStatus())
                .build();
    }
}

