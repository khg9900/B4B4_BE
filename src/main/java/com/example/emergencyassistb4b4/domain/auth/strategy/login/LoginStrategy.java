package com.example.emergencyassistb4b4.domain.auth.strategy.login;

import com.example.emergencyassistb4b4.domain.auth.dto.request.LoginRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.domain.user.domain.LoginType;

public interface LoginStrategy {
    boolean supports( LoginType loginType);
    TokenResponseDto login(LoginRequestDto loginRequestDto);
}
