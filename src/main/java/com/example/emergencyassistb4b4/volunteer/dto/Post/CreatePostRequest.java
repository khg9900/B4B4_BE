package com.example.emergencyassistb4b4.volunteer.dto.Post;

import com.example.emergencyassistb4b4.user.domain.User;
import com.example.emergencyassistb4b4.volunteer.domain.Post;
import com.example.emergencyassistb4b4.volunteer.dto.Post.common.PostAttendancePolicyDto;
import com.example.emergencyassistb4b4.volunteer.dto.Post.common.PostLocationDto;
import com.example.emergencyassistb4b4.volunteer.enums.PostCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @Min(value = 1, message = "최소 모집 인원은 1명 이상이어야 합니다.")
    private int totalCapacity;

    @Min(value = 1, message = "최소 팀 크기는 1명 이상이어야 합니다.")
    private int teamSize;

    @Valid
    @NotNull(message = "위치 정보는 필수입니다.")
    private PostLocationDto location;

    @Valid
    @NotNull(message = "출석 정책은 필수입니다.")
    private PostAttendancePolicyDto attendancePolicy;

    @AssertTrue(message = "전체 인원는 팀 인원으로 나누어 떨어져야 합니다.")
    public boolean isTotalCapacityDivisibleByTeamSize() {
        return teamSize > 0 && totalCapacity % teamSize == 0;
    }

    public Post toEntity(User user) {
        Post post = Post.builder()
                .user(user)
                .category(PostCategory.valueOf(category))
                .title(title)
                .content(content)
                .totalCapacity(totalCapacity)
                .teamSize(teamSize)
                .build();

        post.setLocation(location.toEntity());
        post.setAttendancePolicy(attendancePolicy.toEntity());

        return post;
    }

}