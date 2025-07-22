package com.example.emergencyassistb4b4.global.kafka.dto;

import com.example.emergencyassistb4b4.domain.volunteer.domain.Post;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VolunteerUpdatedEvent {

    private Long postId;
    private String title;
    private String placeName;
    private LocalDateTime checkinStart;

    public static VolunteerUpdatedEvent from(Post post) {

        return VolunteerUpdatedEvent.builder()
            .postId(post.getId())
            .title(post.getTitle())
            .placeName(post.getLocation().getPlaceName())
            .checkinStart(post.getAttendancePolicy().getCheckinStart())
            .build();
    }
}