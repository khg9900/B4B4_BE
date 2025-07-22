package com.example.emergencyassistb4b4.domain.auth.strategy.signup;

import com.example.emergencyassistb4b4.domain.auth.dto.request.SignUpRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.domain.user.domain.LoginType;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;

public interface SignUpStrategy {
    boolean supports(UserRole userRole, LoginType loginType);
    TokenResponseDto signUp(SignUpRequestDto signUpRequest);

}
