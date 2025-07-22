package com.example.emergencyassistb4b4.domain.volunteer.dto.Join;

import java.time.LocalDateTime;

public record CheckinPeriodDto(
        LocalDateTime checkinStart,
        LocalDateTime checkinEnd
) {}