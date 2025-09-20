package com.example.emergencyassistb4b4.domain.location.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ShelterResponseDto {
    private String name;
    private String address;
    private double latitude;
    private double longitude;

    // Kakao Map API 응답(JsonNode)에서 대피소 정보를 추출해 DTO로 변환
    public static ShelterResponseDto from(JsonNode doc) {
        return ShelterResponseDto.builder()
                .name(doc.path("place_name").asText())
                .address(doc.path("address_name").asText())
                .latitude(doc.path("y").asDouble())
                .longitude(doc.path("x").asDouble())
                .build();
    }
}
