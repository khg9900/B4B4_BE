package com.example.emergencyassistb4b4.domain.alert.dto.volunteer;

import com.example.emergencyassistb4b4.domain.alert.domain.volunteer.UserVolunteerAlert;
import com.example.emergencyassistb4b4.domain.alert.domain.volunteer.VolunteerAlert;
import com.example.emergencyassistb4b4.domain.alert.dto.response.UserAlert;
import com.example.emergencyassistb4b4.domain.alert.enums.VolunteerAlertSubtype;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VolunteerAlertResponseDto implements UserAlert {

    private Long id;
    private String title;
    private String placeName;
    private LocalDateTime volunteerDate;
    private VolunteerAlertSubtype subtype;
    private LocalDateTime createdAt;

    public static VolunteerAlertResponseDto fromUserVolunteerAlert(UserVolunteerAlert userVolunteerAlert) {
        VolunteerAlert volunteerAlert = userVolunteerAlert.getVolunteerAlert();

        return VolunteerAlertResponseDto.builder()
            .id(userVolunteerAlert.getId())
            .title(volunteerAlert.getTitle())
            .placeName(volunteerAlert.getPlaceName())
            .volunteerDate(volunteerAlert.getVolunteerDate())
            .subtype(volunteerAlert.getSubtype())
            .createdAt(volunteerAlert.getCreatedAt())
            .build();
    }
}