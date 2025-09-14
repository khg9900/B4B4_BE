package com.example.emergencyassistb4b4.domain.volunteer.dto.Post;

import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostAttendancePolicyDto;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostLocationDto;
import com.example.emergencyassistb4b4.domain.volunteer.dto.validator.ValidAttendancePolicy;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostCategory;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidAttendancePolicy
public class CreatePostRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotNull(message = "봉사일자는 필수입니다.")
    private LocalDate volunteerDate;

    @NotNull(message = "봉사 시작 시간은 필수입니다.")
    private LocalTime volunteerStartTime;

    @NotNull(message = "봉사 종료 시간은 필수입니다.")
    private LocalTime volunteerEndTime;

    @NotNull(message = "모집 시작일은 필수입니다.")
    private LocalDate recruitmentStartDate;

    @NotNull(message = "모집 종료일은 필수입니다.")
    private LocalDate recruitmentEndDate;

    @Min(value = 1, message = "최소 모집 인원은 1명 이상이어야 합니다.")
    private int totalCapacity;

    @Min(value = 1, message = "최소 팀 크기는 1명 이상이어야 합니다.")
    private int teamSize;

    @NotNull(message = "카테고리는 필수입니다.")
    private PostCategory category;

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
                .title(title)
                .content(content)
                .volunteerDate(volunteerDate)
                .volunteerStartTime(volunteerStartTime)
                .volunteerEndTime(volunteerEndTime)
                .recruitmentStartDate(recruitmentStartDate)
                .recruitmentEndDate(recruitmentEndDate)
                .totalCapacity(totalCapacity)
                .teamSize(teamSize)
                .category(category)
                .status(PostStatus.OPEN)
                .build();

        post.setLocation(location.toEntity());
        post.setAttendancePolicy(attendancePolicy.toEntity());

        return post;
    }

}