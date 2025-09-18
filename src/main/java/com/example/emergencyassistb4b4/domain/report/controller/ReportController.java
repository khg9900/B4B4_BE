package com.example.emergencyassistb4b4.domain.report.controller;

import com.example.emergencyassistb4b4.domain.report.dto.*;
import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.status.SuccessStatus;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.domain.report.enums.ReportStatus;
import com.example.emergencyassistb4b4.domain.report.service.ReportService;
import com.example.emergencyassistb4b4.global.security.auth.CustomUserDetails;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    // 사용자: 재난 신고 생성 (텍스트 JSON + 이미지/영상 업로드)
    @PreAuthorize("hasRole('IND')")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ReportResponseDto>> disasterReport(
            @RequestPart("request") String rawJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        User currentUser = userDetails.getUser();

        try {
            // text로 받은 것 객체화
            ReportRequestDto requestDto = objectMapper.readValue(rawJson, ReportRequestDto.class);

            // 필수값 수동 검증 추가 (NPE 방지)
            if (requestDto.getLatitude() == null || requestDto.getLongitude() == null) {
                throw new ApiException(ErrorStatus.REPORT_BAD_REQUEST);
            }

            ReportResponseDto responseDto = reportService.disasterReport(requestDto, currentUser, image, video);

            return ApiResponse.onSuccess(SuccessStatus.REPORT_CREATE_SUCCESS, responseDto);
        } catch (IOException e) {
            throw new ApiException(ErrorStatus.S3_UPLOAD_ERROR);
        }
    }

    // 공공기관: 특정 신고 상태 변경
    @PreAuthorize("hasRole('GOV')")
    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ApiResponse<ReportStatusResponseDto>> changeStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable(name ="reportId") Long reportId,
            @RequestParam ReportStatus newStatus){

        Long publicId = userDetails.getUser().getId();
        ReportStatusResponseDto dto = reportService.changeReportStatus(publicId,reportId,newStatus);

        return ApiResponse.onSuccess(SuccessStatus.REPORT_CREATE_SUCCESS,dto);
    }

    // 공공기관: 주변 신고 목록 조회 (시/구 기준, Slice 페이징)
    @PreAuthorize("hasRole('GOV')")
    @GetMapping("/slice")
    public ResponseEntity<ApiResponse<Slice<ReportDto>>> getNearby
    (
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String si,
            @RequestParam String gu,
            @RequestParam(required = false) ReportStatus status,
            Pageable pageable){

        Slice<ReportDto> slice = reportService.getNearbyReports(si, gu, status, pageable);

        return ApiResponse.onSuccess(SuccessStatus.REPORT_GET_SUCCESS,slice);
    }

    // 사용자: 내가 작성한 신고 목록 조회 (Cursor 페이징)
    @PreAuthorize("hasRole('IND')")
    @GetMapping("/my/cursor")
    public ResponseEntity<ApiResponse<CursorResponse<ReportDto>>> getMyReportsByCursor(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            ReportCursorRequest req
    ) {

        Long userId = userDetails.getUser().getId();
        CursorResponse<ReportDto> resp = reportService.getMyReportsByCursor(userId, sortOrder, req);

        return ApiResponse.onSuccess(SuccessStatus.REPORT_GET_SUCCESS, resp);
    }

    // 공공기관: 접수된 신고 현황 요약 조회
    @PreAuthorize("hasRole('GOV')")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReportStatusCounts>> getReportsSummary(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long publicId = userDetails.getUser().getId();
        ReportStatusCounts resp = reportService.getReportsSummary(publicId);

        return ApiResponse.onSuccess(SuccessStatus.REPORT_GET_SUCCESS, resp);
    }
}
