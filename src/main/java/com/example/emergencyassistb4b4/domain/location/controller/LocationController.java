package com.example.emergencyassistb4b4.domain.location.controller;

import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.domain.location.dto.request.RegionRequestDto;
import com.example.emergencyassistb4b4.domain.location.service.LocationService;
import com.example.emergencyassistb4b4.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.emergencyassistb4b4.global.status.SuccessStatus.LOCATION_SAVE_SUCCESS;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // 유저의 시,구 정보를 저장
    @PostMapping("/region")
    public ResponseEntity<ApiResponse<String>> saveRegion(@RequestBody RegionRequestDto dto,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails){

        locationService.saveRegion(userDetails.getUser().getId(), dto.getProvince(), dto.getCity());

        return ApiResponse.onSuccess(LOCATION_SAVE_SUCCESS,null);
    }

}



