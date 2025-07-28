package com.example.emergencyassistb4b4.domain.auth.service;


import com.example.emergencyassistb4b4.domain.auth.dto.request.LoginRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.request.SignUpRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.domain.auth.token.RedisService;
import com.example.emergencyassistb4b4.domain.auth.token.TokenService;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import com.example.emergencyassistb4b4.domain.user.dto.UserResponseDto;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.security.auth.CustomUserDetails;
import com.example.emergencyassistb4b4.global.security.jwt.JwtUtils;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtils jwtUtils;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public TokenResponseDto login(LoginRequestDto loginRequestDto) {

        // 1. 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        // 2. 인증된 사용자 정보
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserResponseDto userDto = UserResponseDto.from(userDetails.getUser());

        // 3. 토큰 발급
        return tokenService.issueToken(userDto);
    }

    public TokenResponseDto signup(SignUpRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new ApiException(ErrorStatus.DUPLICATED_EMAIL);
        }

        UserRole role = requestDto.getUserRole();

        User.UserBuilder builder = User.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .nickname(requestDto.getName())
                .phoneNumber(requestDto.getPhoneNumber())
                .userRole(role);

        if (role == UserRole.GOV) {
            builder.province(requestDto.getProvince());
            builder.city(requestDto.getCity());
        }

        User user = builder.build();
        userRepository.save(user);

        return tokenService.issueToken(UserResponseDto.from(user));
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
