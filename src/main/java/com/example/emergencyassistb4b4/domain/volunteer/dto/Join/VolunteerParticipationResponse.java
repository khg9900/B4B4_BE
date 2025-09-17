package com.example.emergencyassistb4b4.domain.volunteer.dto.Join;

import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private String province;
    private String city;
    private String placeName;
    private LocalDate volunteerDate;
    private LocalTime volunteerStartTime;
    private LocalTime volunteerEndTime;

    public static VolunteerParticipationResponse from(VolunteerParticipant vp) {
        return VolunteerParticipationResponse.builder()
                .participantId(vp.getId())
                .postId(vp.getVolunteerTeam().getPost().getId())
                .postTitle(vp.getVolunteerTeam().getPost().getTitle())
                .teamNumber(vp.getVolunteerTeam().getTeamNumber())
                .status(vp.getCheckinStatus().name())
                .volunteerDate(vp.getVolunteerTeam().getPost().getVolunteerDate())
                .volunteerStartTime(vp.getVolunteerTeam().getPost().getVolunteerStartTime())
                .volunteerEndTime(vp.getVolunteerTeam().getPost().getVolunteerEndTime())
                .province(vp.getVolunteerTeam().getPost().getLocation().getProvince())
                .city(vp.getVolunteerTeam().getPost().getLocation().getCity())
                .placeName(vp.getVolunteerTeam().getPost().getLocation().getPlaceName())
                .build();
    }
}
