package com.example.emergencyassistb4b4.domain.auth.strategy.signup;

import com.example.emergencyassistb4b4.domain.auth.dto.request.SignUpRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.domain.auth.token.TokenService;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.domain.user.domain.LoginType;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import com.example.emergencyassistb4b4.domain.user.dto.UserResponseDto;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NgoSignUpStrategy implements SignUpStrategy {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenService tokenService;

    @Override
    public boolean supports(UserRole userRole, LoginType loginType) {
        return userRole == UserRole.NGO && loginType == LoginType.LOCAL;
    }

    @Override
    public TokenResponseDto signUp(SignUpRequestDto requestDto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new ApiException(ErrorStatus.DUPLICATED_EMAIL);
        }

        // 사용자 정보 설정
        User user = User.builder()
                .nickname(requestDto.getName())
                .email(requestDto.getEmail())
                .password(bCryptPasswordEncoder.encode(requestDto.getPassword()))
                .phoneNumber(requestDto.getPhoneNumber())
                .loginType(LoginType.LOCAL)
                .userRole(UserRole.NGO)
                .build();

        // 사용자 저장
        userRepository.save(user);

        // 회원가입 후 자동 로그인: 로그인 시점에서 토큰을 발급
        return loginAfterSignUp(user);
    }

    private TokenResponseDto loginAfterSignUp(User user) {
        // 로그인 후 토큰 발급 (즉시 로그인 처리)
        return tokenService.issueToken(UserResponseDto.from(user));
    }
}
