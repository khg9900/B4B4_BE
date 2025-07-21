package com.example.emergencyassistb4b4.domain.alert.client.volunteer;

import java.util.List;

public interface VolunteerParticipantClient {

    List<Long> findParticipantIds(Long postId);

}
