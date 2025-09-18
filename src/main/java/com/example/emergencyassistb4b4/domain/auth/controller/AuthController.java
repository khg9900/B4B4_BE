package com.example.emergencyassistb4b4.domain.auth.controller;

import com.example.emergencyassistb4b4.domain.auth.dto.request.LoginRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.request.SignUpRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.request.TokenReissueRequestDto;
import com.example.emergencyassistb4b4.domain.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.domain.auth.service.AuthService;
import com.example.emergencyassistb4b4.domain.auth.token.TokenService;
import com.example.emergencyassistb4b4.global.response.ApiResponse;
import com.example.emergencyassistb4b4.global.security.jwt.JwtUtils;
import com.example.emergencyassistb4b4.global.status.SuccessStatus;
import jakarta.servlet.ServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final JwtUtils jwtUtils;

    // 회원가입 요청을 받아 유저를 생성하고 토큰을 발급하는 API
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<TokenResponseDto>> signup(
            @Valid
            @RequestBody SignUpRequestDto requestDto, ServletRequest servletRequest) {

        authService.signup(requestDto);

        return ApiResponse.onSuccess(SuccessStatus.SIGNUP_SUCCESS, null);
    }

    // 클라이언트의 아이디와 비밀번호를 검증하고 access token, refresh token 을 발급하는 API
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(
            @Valid
            @RequestBody LoginRequestDto requestDto, ServletRequest servletRequest) {

        TokenResponseDto tokens = authService.login(requestDto);

        return ApiResponse.onSuccess(SuccessStatus.LOGIN_SUCCESS, tokens);
    }

    // 클라이언트의 refresh token을 사용해 access token 과 refresh token 을 재발급 하는 API
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponseDto>> createNewAccessToken(
            @Valid
            @RequestBody TokenReissueRequestDto request) {

        TokenResponseDto tokenResponseDto = tokenService.reissueAccessToken(request.getRefreshToken());

        return ApiResponse.onSuccess(SuccessStatus.TOKEN_REISSUE_SUCCESS, tokenResponseDto);
    }

    // 클라이언트의 access token을 무효화하여 로그아웃 처리하는 API
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(ServletRequest servletRequest) {

        String token = jwtUtils.resolveToken(servletRequest);
        authService.logout(token);

        return ApiResponse.onSuccess(SuccessStatus.LOGOUT_SUCCESS, null);
    }
}
