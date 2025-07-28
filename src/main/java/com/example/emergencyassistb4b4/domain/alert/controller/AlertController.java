package com.example.emergencyassistb4b4.domain.alert.controller;

import com.example.emergencyassistb4b4.domain.alert.dto.response.UserAlert;
import com.example.emergencyassistb4b4.domain.alert.enums.AlertType;
import com.example.emergencyassistb4b4.domain.alert.service.query.AlertQueryService;
import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.security.auth.CustomUserDetails;
import com.example.emergencyassistb4b4.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertQueryService alertQueryService;

    @GetMapping
    @PreAuthorize("hasRole('IND')")
    public ResponseEntity<ApiResponse<List<UserAlert>>> listAlerts(
        @RequestParam String alertType,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(SuccessStatus.ALERTS_GET_SUCCESS,
            alertQueryService.listAlerts(AlertType.from(alertType), userDetails.getUser()));
    }
}