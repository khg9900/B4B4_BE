package com.example.emergencyassistb4b4.auth.strategy.signup;

import com.example.emergencyassistb4b4.auth.dto.request.SignUpRequestDto;
import com.example.emergencyassistb4b4.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.user.domain.LoginType;
import com.example.emergencyassistb4b4.user.domain.UserRole;

public interface SignUpStrategy {
    boolean supports(UserRole userRole, LoginType loginType);
    TokenResponseDto signUp(SignUpRequestDto signUpRequest);

}
