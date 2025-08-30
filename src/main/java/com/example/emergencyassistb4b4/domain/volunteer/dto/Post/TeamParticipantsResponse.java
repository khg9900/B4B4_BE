package com.example.emergencyassistb4b4.domain.volunteer.dto.Post;
import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class TeamParticipantsResponse {
    private Long teamId;
    private int teamNumber;
    private List<ParticipantDto> participants;

    public static TeamParticipantsResponse from(Long teamId, int teamNumber, List<ParticipantDto> participants) {
        return TeamParticipantsResponse.builder()
                .teamId(teamId)
                .teamNumber(teamNumber)
                .participants(participants)
                .build();
    }

    // VolunteerParticipant 리스트를 ParticipantDto로 변환 후 생성
    public static TeamParticipantsResponse fromEntities(Long teamId, int teamNumber, List<VolunteerParticipant> participants) {
        List<ParticipantDto> dtoList = participants.stream()
                .map(ParticipantDto::from)
                .collect(Collectors.toList());
        return from(teamId, teamNumber, dtoList);
    }
}
