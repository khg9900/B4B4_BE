package com.example.emergencyassistb4b4.domain.alert.dto.volunteer;

import com.example.emergencyassistb4b4.domain.alert.domain.volunteer.VolunteerAlert;
import com.example.emergencyassistb4b4.global.kafka.dto.VolunteerUpdatedEvent;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VolunteerUpdateAlertDto {

    private final Long postId;
    private final String title;
    private final String placeName;
    private final LocalDateTime checkinStart;

    public static VolunteerUpdateAlertDto from(VolunteerUpdatedEvent event) {

        return VolunteerUpdateAlertDto.builder()
            .postId(event.getPostId())
            .title(event.getTitle())
            .placeName(event.getPlaceName())
            .checkinStart(event.getCheckinStart())
            .build();
    }

    public VolunteerAlert toEntity() {
        return VolunteerAlert.builder()
            .title(this.title)
            .placeName(this.placeName)
            .checkinStart(this.checkinStart)
            .build();
    }
}
