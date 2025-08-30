package com.example.emergencyassistb4b4.domain.volunteer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostStatus {

    OPEN("모집 중"),
    CLOSED("모집 마감"),
    COMPLETED("봉사 완료");

    private final String displayName;
}
