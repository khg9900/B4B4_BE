package com.example.emergencyassistb4b4.auth.dto.response;

import com.example.emergencyassistb4b4.user.domain.LoginType;
import com.example.emergencyassistb4b4.user.domain.UserRole;

public record LoginResponseDto(
        String email,
        UserRole userRole,
        LoginType loginType
) {
}
