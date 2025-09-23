package com.example.emergencyassistb4b4.domain.user.controller;

import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.security.auth.CustomUserDetails;
import com.example.emergencyassistb4b4.global.status.SuccessStatus;
import com.example.emergencyassistb4b4.domain.user.dto.UserResponseDto;
import com.example.emergencyassistb4b4.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // 로그인한 사용자 본인 정보 조회
    @GetMapping("/my-info")
    public ResponseEntity<ApiResponse<UserResponseDto>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserResponseDto responseDto = userService.getMyInfo(userDetails.getUsername());

        return ApiResponse.onSuccess(SuccessStatus.USER_INFO_GET_SUCCESS, responseDto);
    }
}
