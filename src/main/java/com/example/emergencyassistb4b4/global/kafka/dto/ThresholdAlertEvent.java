package com.example.emergencyassistb4b4.global.kafka.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdAlertEvent {

    private String province;

    private String city;

    private String alertType; // 재난 타입

    private long windowStart; // reportedAt(epoch ms) >> KST로 날짜 산출
}
