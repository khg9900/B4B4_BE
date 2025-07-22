package com.example.emergencyassistb4b4.domain.auth.strategy.login;

import com.example.emergencyassistb4b4.domain.auth.dto.request.LoginRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.response.TokenResponseDto;

public interface LoginStrategy {
    TokenResponseDto login(LoginRequestDto loginRequestDto);

}
