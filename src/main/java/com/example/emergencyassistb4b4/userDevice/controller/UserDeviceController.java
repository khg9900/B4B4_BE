package com.example.emergencyassistb4b4.userDevice.controller;

import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.security.CustomUserDetails;
import com.example.emergencyassistb4b4.global.status.SuccessStatus;
import com.example.emergencyassistb4b4.userDevice.dto.UserDeviceRequestDto;
import com.example.emergencyassistb4b4.userDevice.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveDevice(
        @RequestBody UserDeviceRequestDto dto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userDeviceService.saveDevice(userDetails.getUser(), dto);
        return ApiResponse.onSuccess(SuccessStatus.DEVICE_CREATE_SUCCESS, null);
    }
}