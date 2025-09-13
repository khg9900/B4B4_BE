package com.example.emergencyassistb4b4.domain.location.controller;

import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.domain.location.dto.request.RegionRequestDto;
import com.example.emergencyassistb4b4.domain.location.service.LocationService;
import com.example.emergencyassistb4b4.global.security.auth.CustomUserDetails;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.emergencyassistb4b4.global.status.SuccessStatus.LOCATION_SAVE_SUCCESS;

// 백그라운드 존재, 입력받을 창구로써 역할 -> return 값이 Void로 변경가능성 존재
// 일단 JWT에서 userId 가져올 예정
@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // 모든 유저 저장
    @PostMapping("/region")
    public ResponseEntity<ApiResponse<String>> saveRegion(@RequestBody RegionRequestDto dto,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        //추후 변경 예정
        User currentUser = userDetails.getUser();

        locationService.saveRegion(currentUser.getId(), dto.getProvince(), dto.getCity());


        return ApiResponse.onSuccess(LOCATION_SAVE_SUCCESS,null);
    }

}



