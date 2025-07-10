package com.example.emergencyassistb4b4.alert.client.volunteer;

import com.example.emergencyassistb4b4.volunteer.service.VolunteerParticipantService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VolunteerParticipantClientImpl implements VolunteerParticipantClient {

    private final VolunteerParticipantService volunteerParticipantService;

    @Override
    public List<Long> findParticipantIds(Long postId) {
        return volunteerParticipantService.findParticipants(postId);
    }
}
