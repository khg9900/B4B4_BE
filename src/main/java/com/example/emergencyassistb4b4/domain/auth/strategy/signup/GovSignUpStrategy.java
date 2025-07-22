package com.example.emergencyassistb4b4.domain.auth.strategy.signup;

import com.example.emergencyassistb4b4.domain.auth.dto.request.SignUpRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.domain.auth.token.TokenService;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import com.example.emergencyassistb4b4.domain.user.dto.UserResponseDto;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GovSignUpStrategy implements SignUpStrategy {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenService tokenService;

    @Override
    public boolean supports(UserRole userRole) {

        return userRole == UserRole.GOV;
    }

    @Override
    public TokenResponseDto signUp(SignUpRequestDto requestDto) {

        // 공공 회원가입 로직 구현
        User user = User.builder()
                .nickname(requestDto.getName())
                .email(requestDto.getEmail())
                .password(bCryptPasswordEncoder.encode(requestDto.getPassword()))
                .phoneNumber(requestDto.getPhoneNumber())
                .province(requestDto.getProvince())
                .city(requestDto.getCity())
                .userRole(UserRole.GOV)
                .build();

        userRepository.save(user);

        return loginAfterSignUp(user);
    }

    private TokenResponseDto loginAfterSignUp(User user) {

        // 로그인 후 토큰 발급 (즉시 로그인 처리)
        return tokenService.issueToken(UserResponseDto.from(user));
    }
}
