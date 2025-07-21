package com.example.emergencyassistb4b4.domain.volunteer.controller;

import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.security.CustomUserDetails;
import com.example.emergencyassistb4b4.global.status.SuccessStatus;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.CheckinStatusRequest;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.VolunteerParticipationResponse;
import com.example.emergencyassistb4b4.domain.volunteer.service.VolunteerJoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class VolunteerJoinController {

    private final VolunteerJoinService volunteerJoinService;

    @PreAuthorize("hasRole('IND')")
    @PostMapping("/posts/{postId}/teams/{teamNumber}/apply")
    public ResponseEntity<ApiResponse<Void>> joinTeam(
            @PathVariable Long postId,
            @PathVariable int teamNumber,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        volunteerJoinService.joinTeam(postId, teamNumber, userDetails.getUser().getId());
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_CREATE_SUCCESS, null);
    }

    @PreAuthorize("hasRole('IND')")
    @PatchMapping("/volunteer-participants/{participantId}")
    public  ResponseEntity<ApiResponse<Void>> cancelJoin(
            @PathVariable Long participantId,
            @Valid @RequestBody CheckinStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        volunteerJoinService.cancelJoin(participantId, request, userDetails.getUser().getId());
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_SUCCESS, null);
    }

    @PreAuthorize("hasRole('IND')")
    @GetMapping("/volunteer-participants/my")
    public ResponseEntity<ApiResponse<List<VolunteerParticipationResponse>>> getMyParticipationList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<VolunteerParticipationResponse> list = volunteerJoinService.getMyParticipation(userDetails.getUser().getId());
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_SUCCESS, list);
    }

}