package com.example.emergencyassistb4b4.domain.report.enums;

import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DisasterType {

    EARTHQUAKE("지진"),
    FLOOD("홍수"),
    TYPHOON("태풍"),
    WILDFIRE("산불"),
    LANDSLIDE("산사태"),
    POWER_OUTAGE("정전"),
    TERROR_ATTACK("테러"),
    BUILDING_COLLAPSE("건물 붕괴");

    private final String name;

    public static DisasterType from(String DisasterName) {

        for (DisasterType type : values()) {
            if (type.name().equalsIgnoreCase(DisasterName) || type.name.equals(DisasterName)) {
                return type;
            }
        }
        throw new ApiException(ErrorStatus.REPORT_BAD_REQUEST);
    }
}
