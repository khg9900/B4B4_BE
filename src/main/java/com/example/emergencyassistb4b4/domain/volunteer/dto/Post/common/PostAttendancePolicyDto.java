package com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common;

import com.example.emergencyassistb4b4.domain.volunteer.domain.AttendancePolicy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostAttendancePolicyDto {

    @NotNull(message = "출석 시작 시간은 필수입니다.")
    private LocalDateTime checkinStart;

    @NotNull(message = "출석 종료 시간은 필수입니다.")
    private LocalDateTime checkinEnd;

    @Min(value = 100, message = "출석 허용 반경은 100m 이상이어야 합니다.")
    private int allowedRadiusM;

    @Min(value = 30, message = "최소 출석 시간은 30분 이상이어야 합니다.")
    private int minStayMinutes;

    public AttendancePolicy toEntity() {
        return AttendancePolicy.builder()
                .checkinStart(checkinStart)
                .checkinEnd(checkinEnd)
                .attendanceRadiusMeters(allowedRadiusM)
                .minCheckinMinutes(minStayMinutes)
                .build();
    }

    public static PostAttendancePolicyDto from(AttendancePolicy policy) {
        return PostAttendancePolicyDto.builder()
                .checkinStart(policy.getCheckinStart())
                .checkinEnd(policy.getCheckinEnd())
                .allowedRadiusM(policy.getAttendanceRadiusMeters())
                .minStayMinutes(policy.getMinCheckinMinutes())
                .build();
    }
}