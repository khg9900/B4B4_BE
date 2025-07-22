package com.example.emergencyassistb4b4.domain.auth.strategy.signup;

import com.example.emergencyassistb4b4.domain.auth.dto.request.SignUpRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.domain.auth.token.TokenService;
import com.example.emergencyassistb4b4.domain.user.domain.LoginType;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import com.example.emergencyassistb4b4.domain.user.dto.UserResponseDto;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class KakaoIndSignupStrategy implements SignUpStrategy {
    private final UserRepository userRepository;

    private final TokenService tokenService;


    @Override
    public boolean supports(UserRole userRole, LoginType loginType) {
        return loginType == LoginType.KAKAO && userRole == UserRole.IND;
    }

    @Override
    public TokenResponseDto signUp(SignUpRequestDto requestDto) {
        User user = User.builder()
                .email(requestDto.getEmail())
                .nickname(requestDto.getName())
                .loginType(LoginType.KAKAO)
                .userRole(UserRole.IND)
                .build();
        userRepository.save(user);

        return tokenService.issueToken(UserResponseDto.from(user));
    }
}
