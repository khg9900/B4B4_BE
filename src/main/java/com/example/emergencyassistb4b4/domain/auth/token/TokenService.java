package com.example.emergencyassistb4b4.domain.auth.token;

import com.example.emergencyassistb4b4.domain.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.security.JwtUtils;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.dto.UserResponseDto;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenService {
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public TokenResponseDto reissueAccessToken(String refreshToken) {
        // 1. 리프레시 토큰 유효성 검사
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new ApiException(ErrorStatus.INVAlID_REFRESH_TOKEN);
        }
        // 2. 리프레시 토큰에서 userId 추출
        Long userId = jwtUtils.getUserId(refreshToken);

        // 3. Redis에 저장된 리프레시 토큰과 일치하는지 확인
        String saveRefresh = refreshTokenService.getRefreshToken(userId);
        if (!refreshToken.equals(saveRefresh)) {
           throw new ApiException(ErrorStatus.INVAlID_REFRESH_TOKEN);
        }

        // 4. 유저 정보 조회
        User user = userRepository.findById(userId).orElseThrow( ()-> new ApiException(ErrorStatus.USER_NOT_FOUND));

        // 5. 새로운 access, refresh 토큰 재발급
        String newAccessToken = jwtUtils.generateAccessToken(UserResponseDto.from(user));
        String newRefreshToken = jwtUtils.generateRefreshToken(UserResponseDto.from(user));
        refreshTokenService.saveToken(userId, newRefreshToken); // refresh 토큰 저장

        // 6. 리프레시 토큰 저장 ( 기존 것을 덮어씀)
        refreshTokenService.saveToken(userId, newRefreshToken);

        return new TokenResponseDto(newAccessToken, newRefreshToken);

    }

    public TokenResponseDto issueToken(UserResponseDto user) {

        // 1. Access & Refresh Token 발급
        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        // 2. Refresh 토큰 Redis에 저장
        refreshTokenService.saveToken(user.getId(), refreshToken);

        // 3. Dto 로 변환
        return new TokenResponseDto(accessToken, refreshToken);
    }
}
