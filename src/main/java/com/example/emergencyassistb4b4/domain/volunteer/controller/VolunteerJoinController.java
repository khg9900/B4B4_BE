package com.example.emergencyassistb4b4.domain.volunteer.controller;

import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.VolunteerParticipationFilter;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.security.auth.CustomUserDetails;
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

@PreAuthorize("hasRole('IND')")
@RestController
@RequiredArgsConstructor
@RequestMapping
public class VolunteerJoinController {

    private final VolunteerJoinService volunteerJoinService;

    @PostMapping("/posts/{postId}/teams/{teamNumber}/apply")
    public ResponseEntity<ApiResponse<Void>> joinTeam(
            @PathVariable Long postId,
            @PathVariable int teamNumber,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        volunteerJoinService.joinTeam(postId, teamNumber, userDetails.getUser());
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_APPLY_SUCCESS, null);
    }

    @PatchMapping("/participants/{participantId}")
    public ResponseEntity<ApiResponse<Void>> cancelJoin(
            @PathVariable Long participantId,
            @Valid @RequestBody CheckinStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        volunteerJoinService.cancelJoin(participantId, request, userDetails.getUser());
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_CANCEL_SUCCESS, null);
    }

    @GetMapping("/participants/my")
    public ResponseEntity<ApiResponse<List<VolunteerParticipationResponse>>> getMyParticipationList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute VolunteerParticipationFilter filter
    ) {
        List<VolunteerParticipationResponse> list =
                volunteerJoinService.getMyParticipation(userDetails.getUser().getId(), filter);
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_GET_PARTICIPATION_SUCCESS, list);
    }
}
