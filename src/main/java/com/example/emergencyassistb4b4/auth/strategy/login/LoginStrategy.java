package com.example.emergencyassistb4b4.auth.strategy.login;

import com.example.emergencyassistb4b4.auth.dto.request.LoginRequestDto;
import com.example.emergencyassistb4b4.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.user.domain.LoginType;
import com.example.emergencyassistb4b4.user.domain.UserRole;

public interface LoginStrategy {
    boolean supports( LoginType loginType);
    TokenResponseDto login(LoginRequestDto loginRequestDto);
}
