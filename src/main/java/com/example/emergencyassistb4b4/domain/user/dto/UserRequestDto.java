package com.example.emergencyassistb4b4.domain.user.dto;

import com.example.emergencyassistb4b4.domain.user.domain.LoginType;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRequestDto {
    private Long id;
    private String email;
    private String password;
    private UserRole userRole;
    private LoginType loginType;
}
