package com.example.emergencyassistb4b4.auth.strategy.signup;

import com.example.emergencyassistb4b4.auth.dto.request.SignUpRequestDto;
import com.example.emergencyassistb4b4.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.auth.token.TokenService;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.user.domain.LoginType;
import com.example.emergencyassistb4b4.user.domain.User;
import com.example.emergencyassistb4b4.user.domain.UserRole;
import com.example.emergencyassistb4b4.user.dto.UserResponseDto;
import com.example.emergencyassistb4b4.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IndSignupStrategy implements SignUpStrategy {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenService tokenService;

    @Override
    public boolean supports(UserRole userRole, LoginType loginType) {
        return userRole == UserRole.IND && loginType == LoginType.LOCAL;
    }

    @Override
    public TokenResponseDto signUp(SignUpRequestDto signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new ApiException(ErrorStatus.DUPLICATED_EMAIL);
        }
        // 로컬 개인 회원가입 로직
        User user = User.builder()
                .email(signUpRequest.getEmail())
                .password(bCryptPasswordEncoder.encode(signUpRequest.getPassword()))
                .nickname(signUpRequest.getName())
                .phoneNumber(signUpRequest.getPhoneNumber())
                .loginType(LoginType.LOCAL)
                .userRole(UserRole.IND)
                .build();
        userRepository.save(user);
        return loginAfterSignUp(user);
    }

    private TokenResponseDto loginAfterSignUp(User user) {
        // 로그인 후 토큰 발급 (즉시 로그인 처리)
        return tokenService.issueToken(UserResponseDto.from(user));
    }
}
