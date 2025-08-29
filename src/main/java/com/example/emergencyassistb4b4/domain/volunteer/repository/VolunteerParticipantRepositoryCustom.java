package com.example.emergencyassistb4b4.domain.volunteer.repository;

import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VolunteerParticipantRepositoryCustom {

    List<VolunteerParticipant> findAllByUserIdWithPostAndTeam(
            Long userId,
            CheckinStatus status,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}
