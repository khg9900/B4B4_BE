package com.example.emergencyassistb4b4.domain.volunteer.dto.Post;

import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostAttendancePolicyDto;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostLocationDto;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePostRequest {

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

    @NotNull(message = "상태는 필수입니다.")
    private PostStatus status;

    @Valid
    private PostLocationDto location;

    @Valid
    private PostAttendancePolicyDto attendancePolicy;

}