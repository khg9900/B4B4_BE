package com.example.emergencyassistb4b4.domain.volunteer.repository;

import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Join.VolunteerParticipationFilter;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface VolunteerParticipantRepositoryCustom {

    List<VolunteerParticipant> getMyParticipation(
            Long userId,
            VolunteerParticipationFilter filter
    );
}
