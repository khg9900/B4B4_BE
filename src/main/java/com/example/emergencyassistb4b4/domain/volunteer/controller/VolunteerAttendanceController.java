package com.example.emergencyassistb4b4.domain.volunteer.controller;

import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.CheckinStatusRequest;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.TeamParticipantsResponse;
import com.example.emergencyassistb4b4.domain.volunteer.service.VolunteerPostService;
import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.security.auth.CustomUserDetails;
import com.example.emergencyassistb4b4.global.status.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("posts/{postId}/teams/{teamId}")
public class VolunteerAttendanceController {

    private final VolunteerPostService volunteerPostService;

    /**
     * 특정 팀 참여자 리스트 조회
     */
    @PreAuthorize("hasRole('NGO')")
    @GetMapping
    public ResponseEntity<ApiResponse<TeamParticipantsResponse>> getTeamList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PathVariable Long teamId
    ) {
        TeamParticipantsResponse teamParticipantsResponse = volunteerPostService.getTeamParticipants(postId, teamId);
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_INFORMATION_SUCCESS, teamParticipantsResponse);
    }

    /**
     * 결석 처리된 참여자 출석 상태 변경
     */
    @PreAuthorize("hasRole('NGO')")
    @PatchMapping("volunteer-participants/{participantId}")
    public ResponseEntity<ApiResponse<Void>> patchAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PathVariable Long teamId,
            @PathVariable Long participantId,
            @Valid @RequestBody CheckinStatusRequest checkinStatusRequest
    ) {
        // service에서 상태 변경
        volunteerPostService.updateParticipantAttendance(postId, teamId, participantId,checkinStatusRequest);
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_STATUS_SUCCESS, null);
    }
}
