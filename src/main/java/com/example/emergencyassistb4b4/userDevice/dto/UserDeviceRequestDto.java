package com.example.emergencyassistb4b4.userDevice.dto;

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
    private String fcmToken;
}
