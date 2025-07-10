package com.example.emergencyassistb4b4.auth.strategy.login;

import com.example.emergencyassistb4b4.auth.dto.request.LoginRequestDto;
import com.example.emergencyassistb4b4.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.auth.token.TokenService;
import com.example.emergencyassistb4b4.global.security.CustomUserDetails;
import com.example.emergencyassistb4b4.user.domain.LoginType;
import com.example.emergencyassistb4b4.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeneralLoginStrategy implements LoginStrategy {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Override
    public boolean supports(LoginType loginType) {
        return loginType == LoginType.LOCAL;
    }

    @Override
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {

        // 1. 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword()));

        // 2. 인증된 사용자 정보
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserResponseDto userDto = UserResponseDto.from(userDetails.getUser()); // 이미 DTO 타입이면 바로 사용

        // 3. 토큰 발급은 TokenService가 함
        return tokenService.issueToken(userDto);
    }
}
