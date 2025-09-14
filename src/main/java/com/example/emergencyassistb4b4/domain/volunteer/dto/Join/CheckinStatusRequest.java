package com.example.emergencyassistb4b4.domain.volunteer.dto.Join;

import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckinStatusRequest {

    @NotNull(message = "상태는 필수입니다.")
    private CheckinStatus status;

}