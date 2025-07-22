package com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common;

import com.example.emergencyassistb4b4.domain.volunteer.domain.VolunteerLocation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLocationDto {

    @NotBlank(message = "장소 이름은 필수입니다.")
    private String placeName;

    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;

    public VolunteerLocation toEntity() {
        return VolunteerLocation.builder()
                .placeName(placeName)
                .locationLat(latitude)
                .locationLng(longitude)
                .build();
    }

    public static PostLocationDto from(VolunteerLocation location) {
        return PostLocationDto.builder()
                .placeName(location.getPlaceName())
                .latitude(location.getLocationLat())
                .longitude(location.getLocationLng())
                .build();
    }
}
