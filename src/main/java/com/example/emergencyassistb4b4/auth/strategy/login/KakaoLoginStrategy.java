package com.example.emergencyassistb4b4.auth.strategy.login;

import com.example.emergencyassistb4b4.auth.dto.request.LoginRequestDto;
import com.example.emergencyassistb4b4.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.auth.oauth.dto.KakaoUserDetailsDto;
import com.example.emergencyassistb4b4.auth.oauth.service.KakaoService;
import com.example.emergencyassistb4b4.auth.token.TokenService;
import com.example.emergencyassistb4b4.user.domain.LoginType;
import com.example.emergencyassistb4b4.user.domain.User;
import com.example.emergencyassistb4b4.user.domain.UserRole;
import com.example.emergencyassistb4b4.user.dto.UserResponseDto;
import com.example.emergencyassistb4b4.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoLoginStrategy implements LoginStrategy {
    private final KakaoService kakaoService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    @Override
    public boolean supports(LoginType loginType) {
        return loginType == LoginType.KAKAO;
    }

    @Override
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        //카카오 액세스 토큰을 이용해 사용자 정보 가져오기
        String accessToken = loginRequestDto.getAccessToken();
        // 2. 카카오 사용자 정보로 회원가입 또는 로그인 처리
        KakaoUserDetailsDto kakaoUserDetailsDto = kakaoService.getKakaoUserInfo(accessToken);

        User user = userRepository.findByEmail(kakaoUserDetailsDto.getEmail())
                .orElseGet( () -> {
                    return userRepository.save(
                            User.builder()
                                    .email(kakaoUserDetailsDto.getEmail())  // 카카오에서 받은 이메일F
                                    .nickname(kakaoUserDetailsDto.getNickname())  // 카카오에서 받은 닉네임
                                    .loginType(LoginType.KAKAO)  // 카카오 로그인
                                    .provider("kakao")  // 카카오 제공자
                                    .userRole(UserRole.IND)  // 기본적으로 IND 역할로 설정
                                    .build()
                    );
                });


        // 3. JWT 토큰 발급
        TokenResponseDto tokens = tokenService.issueToken(new UserResponseDto(user.getId(), user.getEmail(), UserRole.IND));
        return tokens;
    }
}
