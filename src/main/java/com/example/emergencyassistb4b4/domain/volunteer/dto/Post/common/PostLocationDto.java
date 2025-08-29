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

    @NotBlank(message = "시/도를 선택해 주세요.")
    private String province;

    private String city;

    @NotBlank(message = "상세 주소명은 필수입니다.")
    private String placeName;

    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;

    public VolunteerLocation toEntity() {
        return VolunteerLocation.builder()
                .province(province)
                .city(city)
                .placeName(placeName)
                .locationLat(latitude)
                .locationLng(longitude)
                .build();
    }

    public static PostLocationDto from(VolunteerLocation location) {
        return PostLocationDto.builder()
                .province(location.getProvince())
                .city(location.getCity())
                .placeName(location.getPlaceName())
                .latitude(location.getLocationLat())
                .longitude(location.getLocationLng())
                .build();
    }
}
