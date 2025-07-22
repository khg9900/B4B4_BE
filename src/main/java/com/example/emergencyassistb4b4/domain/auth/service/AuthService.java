package com.example.emergencyassistb4b4.domain.auth.service;


import com.example.emergencyassistb4b4.domain.auth.dto.request.LoginRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.request.SignUpRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.domain.auth.strategy.login.LoginStrategy;
import com.example.emergencyassistb4b4.domain.auth.strategy.signup.SignUpStrategy;
import com.example.emergencyassistb4b4.domain.auth.token.RedisService;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.security.JwtUtils;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final List<LoginStrategy> loginStrategyList;
    private final JwtUtils jwtUtils;
    private final RedisService redisService;
    private final List<SignUpStrategy>  signUpStrategyList;

    public TokenResponseDto login(LoginRequestDto loginRequestDto) {


        return loginStrategyList.stream()
                .filter(strategy -> strategy.supports(loginRequestDto.getLoginType()))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorStatus.LOGIN_STRATEGY_NOT_FOUND))
                .login(loginRequestDto);
    }

    public TokenResponseDto signup(SignUpRequestDto requestDto) {
        return signUpStrategyList.stream()
                .filter(strategy -> strategy.supports(requestDto.getUserRole(), requestDto.getLoginType()))
                .findFirst()
                .orElseThrow( () -> new ApiException(ErrorStatus.SIGNUP_STRATEGY_NOT_FOUND))
                .signUp(requestDto);
    }

    public void logout(String token) {
        // 1. token 유효성 검사
        if (!jwtUtils.validateToken(token)) {
            throw new ApiException(ErrorStatus.INVALID_ACCESS_TOKEN);
        }
        // 2. 토큰 사용자 이메일 추출
        String email = jwtUtils.getEmailFromToken(token);

        // 3. RefreshToken Redis에서 제거
        redisService.deleteRefreshToken(email);

        // 4. AccessToken 블랙리스트 등록
        long expiration = jwtUtils.getRemainingExpiration(token);
        redisService.addToBlackList(token, expiration);
    }
}
