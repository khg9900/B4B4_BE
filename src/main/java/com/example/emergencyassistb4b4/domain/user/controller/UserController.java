package com.example.emergencyassistb4b4.domain.user.controller;

import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.security.auth.CustomUserDetails;
import com.example.emergencyassistb4b4.global.status.SuccessStatus;
import com.example.emergencyassistb4b4.domain.report.service.ReportService;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.dto.UserInfoResponseDto;
import com.example.emergencyassistb4b4.domain.user.dto.UserResponseDto;
import com.example.emergencyassistb4b4.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final ReportService reportService;

    @GetMapping("/{reportId}/reporter")
    @PreAuthorize("hasRole('Role_GOV')") // 공공기관만 접근 가능
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> getReporterInfo(
            @PathVariable Long reportId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        // 현재 공공기관 사용자 정보 가져오기
        User currentUser = userDetails.getUser();

        // DTO 반환을 서비스에서 처리
        UserInfoResponseDto responseDto = userService.getReporterInfoDto(reportId, currentUser);

        return ApiResponse.onSuccess(SuccessStatus.REPORT_REPORTER_GET_SUCCESS, responseDto);
    }

    @GetMapping("/my-info")
    public ResponseEntity<ApiResponse<UserResponseDto>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserResponseDto responseDto = userService.getMyInfo(userDetails.getUsername());

        return ApiResponse.onSuccess(SuccessStatus.CUSTOM_SUCCESS_STATUS, responseDto);
    }
}
