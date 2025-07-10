package com.example.emergencyassistb4b4.location.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegionRequestDto {

    private String province;
    private String city;
}