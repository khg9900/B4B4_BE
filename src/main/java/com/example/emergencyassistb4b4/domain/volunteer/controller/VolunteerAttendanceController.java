// ==============================
// VolunteerAttendanceController
// ==============================
package com.example.emergencyassistb4b4.domain.volunteer.controller;

import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.CheckinStatusRequest;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.TeamParticipantsResponse;
import com.example.emergencyassistb4b4.domain.volunteer.service.VolunteerPostService;
import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.status.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('NGO')")
@RestController
@RequiredArgsConstructor
@RequestMapping("posts/{postId}/teams/{teamId}")
public class VolunteerAttendanceController {

    private final VolunteerPostService volunteerPostService;

    @GetMapping
    public ResponseEntity<ApiResponse<TeamParticipantsResponse>> getTeamList(
            @PathVariable Long postId,
            @PathVariable Long teamId
    ) {
        TeamParticipantsResponse response = volunteerPostService.getTeamParticipants(postId, teamId);
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_INFORMATION_SUCCESS, response);
    }

    @PatchMapping("volunteer-participants/{participantId}")
    public ResponseEntity<ApiResponse<Void>> patchAttendance(
            @PathVariable Long postId,
            @PathVariable Long teamId,
            @PathVariable Long participantId,
            @Valid @RequestBody CheckinStatusRequest checkinStatusRequest
    ) {
        volunteerPostService.updateParticipantAttendance(postId, teamId, participantId, checkinStatusRequest);
        return ApiResponse.onSuccess(SuccessStatus.VOLUNTEER_STATUS_SUCCESS, null);
    }
}
