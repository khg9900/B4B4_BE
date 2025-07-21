package com.example.emergencyassistb4b4.domain.auth.dto.response;

import com.example.emergencyassistb4b4.domain.user.domain.LoginType;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;

public record LoginResponseDto(
        String email,
        UserRole userRole,
        LoginType loginType
) {
}
