package com.example.emergencyassistb4b4.global.kafka.dto;

import com.example.emergencyassistb4b4.domain.alert.enums.VolunteerAlertSubtype;
import java.time.LocalDateTime;

public interface VolunteerEvent {

    Long getPostId();
    String getTitle();
    String getPlaceName();
    LocalDateTime getVolunteerDate();
    VolunteerAlertSubtype getSubtype();
}
