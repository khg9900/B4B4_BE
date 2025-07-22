package com.example.emergencyassistb4b4.domain.location.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CoordinateRequestDto {
    // userId는 삭제예정
    private Long userId;
    private double latitude;
    private double longitude;
}
