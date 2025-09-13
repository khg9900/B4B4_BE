package com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common;

import com.example.emergencyassistb4b4.domain.volunteer.domain.AttendancePolicy;
import com.example.emergencyassistb4b4.domain.volunteer.dto.validator.ValidAttendancePolicy;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidAttendancePolicy
public class PostAttendancePolicyDto implements AttendancePolicyProvider {

    @NotNull(message = "출석 시작 시간은 필수입니다.")
    private LocalDateTime checkinStart;

    @NotNull(message = "출석 종료 시간은 필수입니다.")
    private LocalDateTime checkinEnd;

    @Min(value = 100, message = "출석 허용 반경은 100m 이상이어야 합니다.")
    private int allowedRadiusM;

    @Override
    public AttendancePolicy getAttendancePolicy() {
        return toEntity();
    }

    public AttendancePolicy toEntity() {
        return AttendancePolicy.builder()
                .checkinStart(checkinStart)
                .checkinEnd(checkinEnd)
                .attendanceRadiusMeters(allowedRadiusM)
                .build();

    }

    public static PostAttendancePolicyDto from(AttendancePolicy policy) {
        return PostAttendancePolicyDto.builder()
                .checkinStart(policy.getCheckinStart())
                .checkinEnd(policy.getCheckinEnd())
                .allowedRadiusM(policy.getAttendanceRadiusMeters())
                .build();
    }

}
