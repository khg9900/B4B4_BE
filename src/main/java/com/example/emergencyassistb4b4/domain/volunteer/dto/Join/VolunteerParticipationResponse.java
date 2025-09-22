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

        var team = vp.getVolunteerTeam();
        var post = team.getPost();
        var location = post.getLocation();

        return VolunteerParticipationResponse.builder()
            .participantId(vp.getId())
            .postId(post.getId())
            .postTitle(post.getTitle())
            .teamNumber(team.getTeamNumber())
            .status(vp.getCheckinStatus().name())
            .volunteerDate(post.getVolunteerDate())
            .volunteerStartTime(post.getVolunteerStartTime())
            .volunteerEndTime(post.getVolunteerEndTime())
            .province(location.getProvince())
            .city(location.getCity())
            .placeName(location.getPlaceName())
            .build();
    }
}
