package com.example.emergencyassistb4b4.domain.volunteer.dto.Post;

import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ParticipantDto {
    private Long participantId;
    private String name;
    private String email;
    private String phone;
    private String status; // PRESENT, ABSENT

    public static ParticipantDto from(VolunteerParticipant participant) {
        return ParticipantDto.builder()
                .participantId(participant.getId())
                .name(participant.getUser().getNickname())
                .email(participant.getUser().getEmail())
                .phone(participant.getUser().getPhoneNumber())
                .status(participant.getCheckinStatus().name())
                .build();
    }
}
