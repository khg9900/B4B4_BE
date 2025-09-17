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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.time.LocalDateTime;
import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ReportResponseDto>> disasterReport(
            // @RequestPart("request") ReportRequestDto requestDto,
            @RequestPart("request") String rawJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        System.out.println("📥 rawJson = " + rawJson);
        System.out.println("🖼 image = " + (image != null ? image.getOriginalFilename() : "없음"));
        System.out.println("🎞 video = " + (video != null ? video.getOriginalFilename() : "없음"));

        User currentUser = userDetails.getUser();

        try {
            ReportRequestDto requestDto = objectMapper.readValue(rawJson, ReportRequestDto.class); // text로 받은 것 객체화

            // 🔻 필수값 수동 검증 추가 (NPE 방지)
            if (requestDto.getLatitude() == null || requestDto.getLongitude() == null) {
                throw new ApiException(ErrorStatus.REPORT_BAD_REQUEST);
            }

            ReportResponseDto responseDto = reportService.disasterReport(requestDto, currentUser, image, video);

            return ApiResponse.onSuccess(SuccessStatus.REPORT_CREATE_SUCCESS, responseDto);

        } catch (IOException e) {
            e.printStackTrace(); // ❗ 실제 예외 로그 보기
            // 전역 처리기로 넘김
            throw new ApiException(ErrorStatus.S3_UPLOAD_ERROR);
        }
    }


    @GetMapping()
    public ResponseEntity<ApiResponse<List<ReportResponseDto>>> getReportList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        // 로그인 사용자 정보 가져오기
        User currentUser = userDetails.getUser();

        List<ReportResponseDto> responseDtos = reportService.getReportList(currentUser);

        return ApiResponse.onSuccess(SuccessStatus.REPORT_GET_SUCCESS, responseDtos);
    }


    // 공공기관 : 단건 상태 변경
    @PreAuthorize("hasRole('GOV')") // AOP
    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ApiResponse<ReportStatusResponseDto>> changeStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable(name ="reportId") Long reportId,
            @RequestParam ReportStatus newStatus){
        Long publicId = userDetails.getUser().getId();  //로그인한 공공기관Id
        ReportStatusResponseDto dto = reportService.changeReportStatus(publicId,reportId,newStatus);
        return ApiResponse.onSuccess(SuccessStatus.REPORT_CREATE_SUCCESS,dto);
    }
    // 공공기관 : 다건 상태변경


    /**  공공기관용 주변 신고목록조회 (지역별(시,구) ,최신순 , Slice 페이징)*/
    @PreAuthorize("hasRole('GOV')")
    @GetMapping("/slice")
    public ResponseEntity<ApiResponse<Slice<ReportDto>>> getNearby
    (
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String si,
            @RequestParam String gu,
            @RequestParam(required = false) ReportStatus status,
            Pageable pageable){
        Long userId = userDetails.getUser().getId();
        Slice<ReportDto> slice = reportService.getNearbyReports(si, gu, status, pageable);

        return ApiResponse.onSuccess(SuccessStatus.REPORT_GET_SUCCESS,slice);
    }


    /** 내 신고 목록 조회  hasRole을 뺴면되는지?*/
//    @PreAuthorize("hasRole('IND')")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Slice<ReportDto>>> getMyReports(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end,
            Pageable pageable
    ) {
        Long userId = userDetails.getUser().getId();
        return ApiResponse.onSuccess(SuccessStatus.REPORT_GET_SUCCESS, reportService.getMyReports(userId, status, start
                , end, pageable));
    }

    /** 공공기관용 주변 신고 목록 (Cursor) */
    @PreAuthorize("hasRole('GOV')")
    @GetMapping("/cursor")
    public ResponseEntity<ApiResponse<CursorResponse<ReportDto>>> getNearbyByCursor(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            ReportCursorRequest req
    ) {
        CursorResponse<ReportDto> resp = reportService.getNearbyReportsByCursor(req);
        return ApiResponse.onSuccess(SuccessStatus.REPORT_GET_SUCCESS, resp);
    }

    /** 내 신고 목록 (Cursor) */
    @GetMapping("/my/cursor")
    @PreAuthorize("hasRole('IND')")
    public ResponseEntity<ApiResponse<CursorResponse<ReportDto>>> getMyReportsByCursor(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            ReportCursorRequest req
    ) {
        Long userId = userDetails.getUser().getId();
        CursorResponse<ReportDto> resp = reportService.getMyReportsByCursor(userId, sortOrder, req);
        return ApiResponse.onSuccess(SuccessStatus.REPORT_GET_SUCCESS, resp);
    }

    @PreAuthorize("hasRole('GOV')")
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayReportStatusCounts>> getReportsSummary(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long publicId = userDetails.getUser().getId();
        TodayReportStatusCounts resp = reportService.getReportsSummary(publicId);
        return ApiResponse.onSuccess(SuccessStatus.REPORT_GET_SUCCESS, resp);
    }
}
