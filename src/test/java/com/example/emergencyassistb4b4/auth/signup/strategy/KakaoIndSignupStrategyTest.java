//package com.example.emergencyassistb4b4.auth.signup.strategy;
//
//import com.example.emergencyassistb4b4.domain.auth.dto.request.SignUpRequestDto;
//import com.example.emergencyassistb4b4.domain.auth.dto.response.TokenResponseDto;
//import com.example.emergencyassistb4b4.domain.auth.token.TokenService;
//import com.example.emergencyassistb4b4.domain.user.domain.User;
//import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
//import com.example.emergencyassistb4b4.domain.user.dto.UserResponseDto;
//import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//public class KakaoIndSignupStrategyTest {
//    private UserRepository userRepository;
//    private TokenService tokenService;
//    private KakaoIndSignupStrategy kakaoIndSignupStrategy;
//
//    @BeforeEach
//    void setUp() {
//        userRepository = mock(UserRepository.class);
//        tokenService = mock(TokenService.class);
//        kakaoIndSignupStrategy = new KakaoIndSignupStrategy(userRepository, tokenService);
//    }
//    @Test
//    public void kakaoSignupSuccess()  throws Exception {
//        // given
//        SignUpRequestDto signUpRequestDto = SignUpRequestDto.builder()
//                .email("kakako@example.com")
//                .name("kakaoUser")
//                .loginType(LoginType.KAKAO)
//                .userRole(UserRole.IND)
//                .build();
//        when(userRepository.existsByEmail("kakako@example.com")).thenReturn(false);
//
//        User savedUser = User.builder()
//                .id(1L)
//                .email(signUpRequestDto.getEmail())
//                .nickname(signUpRequestDto.getName())
//                .userRole(UserRole.IND)
//                .loginType(LoginType.KAKAO)
//                .build();
//
//        when(userRepository.save(any(User.class))).thenReturn(savedUser);
//        when(tokenService.issueToken(any(UserResponseDto.class)))
//                .thenReturn(new TokenResponseDto("access-token", "refresh-token"));
//
//        // when
//        TokenResponseDto result = kakaoIndSignupStrategy.signUp(signUpRequestDto);
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.accessToken()).isEqualTo("access-token");
//        assertThat(result.refreshToken()).isEqualTo("refresh-token");
//
//
//    }
//}