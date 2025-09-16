package com.example.emergencyassistb4b4.global.kafka.dto;

import com.example.emergencyassistb4b4.domain.alert.enums.VolunteerAlertSubtype;
import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VolunteerCancelEvent implements VolunteerEvent {

    private VolunteerAlertSubtype subtype;

    private Long postId;

    private String title;

    private String placeName;

    private LocalDateTime volunteerDate;

    public static VolunteerCancelEvent from(Post post) {

        return VolunteerCancelEvent.builder()
                .subtype(VolunteerAlertSubtype.CANCELED)
                .postId(post.getId())
                .title(post.getTitle())
                .placeName(post.getLocation().getPlaceName())
                .volunteerDate(LocalDateTime.of(post.getVolunteerDate(), post.getVolunteerStartTime()))
                .build();
    }
}
