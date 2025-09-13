package com.example.emergencyassistb4b4.domain.alert.dto.volunteer;

import com.example.emergencyassistb4b4.domain.alert.domain.volunteer.VolunteerAlert;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerCancelEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@Builder
public class VolunteerCancelAlertDto {
    private final Long postId;
    private final String title;
    private final String placeName;
    private LocalDateTime volunteerDate;
    private String subtype;

    public static VolunteerCancelAlertDto  from(VolunteerCancelEvent event,String subtype) {

        return  VolunteerCancelAlertDto .builder()
                .postId(event.getPostId())
                .title(event.getTitle())
                .placeName(event.getPlaceName())
                .volunteerDate(event.getVolunteerDate())
                .subtype(subtype)
                .build();
    }

    public VolunteerAlert toEntity() {
        return VolunteerAlert.builder()
                .title(this.title)
                .placeName(this.placeName)
                .volunteerDate(this.volunteerDate)
                .subtype(this.subtype)
                .build();
    }
}
