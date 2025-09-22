package com.example.emergencyassistb4b4.domain.userDevice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDeviceRequestDto {

    private String type;

    private String os;

    private String osVersion;

    private String model;

    @NotBlank(message = "FCM 토큰 정보는 필수입니다.")
    private String fcmToken;

}
