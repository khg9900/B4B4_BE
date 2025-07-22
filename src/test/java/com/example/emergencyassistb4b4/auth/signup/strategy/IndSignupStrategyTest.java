package com.example.emergencyassistb4b4.auth.signup.strategy;

import com.example.emergencyassistb4b4.domain.auth.dto.request.SignUpRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.domain.auth.strategy.signup.IndSignupStrategy;
import com.example.emergencyassistb4b4.domain.auth.token.TokenService;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import com.example.emergencyassistb4b4.domain.user.dto.UserResponseDto;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndSignupStrategyTest {
    @InjectMocks
    private IndSignupStrategy indSignupStrategy;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenService tokenService;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    public void LocalSignupSuccess()  throws Exception {
        // given
        SignUpRequestDto request = SignUpRequestDto.builder()
                .email("test@local.com")
                .password("secure123!")
                .name("tester")
                .loginType(LoginType.LOCAL)
                .userRole(UserRole.IND)
                .build();


        User user = User.builder()
                .id(1L)
                .email(request.getEmail())
                .password("encodedPw")
                .nickname(request.getName())
                .loginType(LoginType.LOCAL)
                .userRole(UserRole.IND)
                .build();


        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenService.issueToken(any(UserResponseDto.class)))
                .thenReturn(new TokenResponseDto("access-token", "refresh-token"));

        // when
        TokenResponseDto result = indSignupStrategy.signUp(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
    }

}