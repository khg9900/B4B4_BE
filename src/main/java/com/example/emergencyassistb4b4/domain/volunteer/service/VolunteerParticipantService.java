package com.example.emergencyassistb4b4.domain.volunteer.service;

import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerTeam;
import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.example.emergencyassistb4b4.domain.volunteer.repository.VolunteerParticipantRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VolunteerParticipantService {

    private final VolunteerParticipantRepository participantRepository;

    @Transactional
    public VolunteerParticipant joinSave(User user, VolunteerTeam team) {
        VolunteerParticipant participant = VolunteerParticipant.builder()
                .user(user)
                .volunteerTeam(team)
                .joinedAt(LocalDateTime.now())
                .checkinStatus(CheckinStatus.PARTICIPATED)
                .build();

        participant = participantRepository.save(participant);


        return participant;
    }

    public List<Long> findParticipants(Long postId) {
        return participantRepository.findUserIdsByPostId(postId);
    }

}