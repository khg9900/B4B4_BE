package com.example.emergencyassistb4b4.domain.location.controller;

import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.domain.location.dto.response.DisasterSummaryDto;
import com.example.emergencyassistb4b4.domain.location.dto.response.ShelterResponseDto;
import com.example.emergencyassistb4b4.domain.location.service.KakaoMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.emergencyassistb4b4.global.status.SuccessStatus.DISASTER_SEARCH_SUCCESS;
import static com.example.emergencyassistb4b4.global.status.SuccessStatus.SHELTER_SEARCH_SUCCESS;

@RestController
@RequiredArgsConstructor
public class KakaoMapController {


    private final KakaoMapService kakaoMapService;

    // 쿼리 파라미터로 위도, 경도, 반경을 받아 대피소 목록 조회
    @GetMapping("/shelters")
    public ResponseEntity<ApiResponse<List<ShelterResponseDto>>> getNearbyShelters(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "1000") double radiusMeter) {

        List<ShelterResponseDto> shelterResponseDtoList =kakaoMapService.searchShelters(latitude, longitude, radiusMeter);

        return ApiResponse.onSuccess(SHELTER_SEARCH_SUCCESS, shelterResponseDtoList);
    }

    // 쿼리 파라미터로 위도, 경도, 반경을 받아 재난 목록 조회
    @GetMapping("/reports/map")
    public ResponseEntity<ApiResponse<List<DisasterSummaryDto>>> getDisasterSummary(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "1000") int radiusMeter,
            @RequestParam(defaultValue = "3600") long secondsAgo) {



        List<DisasterSummaryDto> summary = kakaoMapService.getDisasterSummary(
                latitude, longitude, radiusMeter, secondsAgo
        );

        return ApiResponse.onSuccess(DISASTER_SEARCH_SUCCESS, summary);
    }

}


