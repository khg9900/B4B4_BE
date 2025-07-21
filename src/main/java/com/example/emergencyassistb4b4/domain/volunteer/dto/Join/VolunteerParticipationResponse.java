package com.example.emergencyassistb4b4.domain.volunteer.dto.Join;

import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VolunteerParticipationResponse {

    private Long participantId;
    private Long postId;
    private String postTitle;
    private int teamNumber;
    private String status;
    private LocalDateTime joinedAt;
    private LocalDateTime checkinStart;
    private LocalDateTime checkinEnd;
    private String placeName;

    public static VolunteerParticipationResponse from(VolunteerParticipant vp) {
        return VolunteerParticipationResponse.builder()
                .participantId(vp.getId())
                .postId(vp.getVolunteerTeam().getPost().getId())
                .postTitle(vp.getVolunteerTeam().getPost().getTitle())
                .teamNumber(vp.getVolunteerTeam().getTeamNumber())
                .status(vp.getCheckinStatus().name())
                .joinedAt(vp.getJoinedAt())
                .checkinStart(vp.getVolunteerTeam().getPost().getAttendancePolicy().getCheckinStart())
                .checkinEnd(vp.getVolunteerTeam().getPost().getAttendancePolicy().getCheckinEnd())
                .placeName(vp.getVolunteerTeam().getPost().getLocation().getPlaceName())
                .build();
    }
}
