package com.example.emergencyassistb4b4.domain.volunteer.dto.Join;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatusDto {
    private Long teamId;
    private int teamNumber;
    private int maxCapacity;
    private int currentCount;
}