package com.example.emergencyassistb4b4.domain.auth.oauth.handler;

import com.example.emergencyassistb4b4.domain.auth.dto.response.TokenResponseDto;
import com.example.emergencyassistb4b4.domain.auth.oauth.dto.KakaoUserDetailsDto;
import com.example.emergencyassistb4b4.domain.auth.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.example.emergencyassistb4b4.domain.auth.oauth.service.KakaoService;
import com.example.emergencyassistb4b4.domain.auth.token.TokenService;
import com.example.emergencyassistb4b4.global.util.CookieUtil;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import com.example.emergencyassistb4b4.domain.user.dto.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler  extends SimpleUrlAuthenticationSuccessHandler {
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(1);
    public static final String REDIRECT_URI = "http://10.0.2.2:8080/oauth2/success";

    private final TokenService tokenService;
    private final KakaoService kakaoService;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository oauth2AuthorizationRequestBasedOnCookieRepository;



    /**
     * OAuth2 인증 성공 시 호출되는 메서드
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {


        OAuth2User oAuth2User  = (OAuth2User) authentication.getPrincipal();   // 인증 성공 객체에서 OAuth2UserPrincipal을 가져옴
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = oAuth2User.getName();

        if("kakao".equalsIgnoreCase(registrationId)) {

            // 카카오 액세스 토큰 얻기
            String accessToken = (String) attributes.get("access_token");

            // 카카오 사용자 정보 가져오기
            KakaoUserDetailsDto kakaoUserDetails = kakaoService.getKakaoUserInfo(accessToken);

            // 사용자 정보를 바탕으로 토큰 발급
            TokenResponseDto kakaoToken = tokenService.issueToken(new UserResponseDto(kakaoUserDetails.getId(), kakaoUserDetails.getEmail(), UserRole.IND));

            //  리프레시 토큰 쿠키 저장
            addRefreshTokenToCookie(request, response, kakaoToken.refreshToken());

            // 인증 관련 설정값, 쿠키 제거
            clearAuthenticationAttributes(request, response);

            // 리다이렉트 처리
            String targetUrl = getTargetUrl(kakaoToken);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);


        } else if ("google".equalsIgnoreCase(registrationId)) {
            String refreshToken = (String) attributes.get("access_token");
            //  GoogleUserDetails googleUserDetails = googleService.getGoogleUserInfo(accessToken);  // 구글 사용자 정보 가져오기
        }


    }

    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);
        CookieUtil.addCookie(response, REFRESH_TOKEN, refreshToken, cookieMaxAge);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request,
        HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        oauth2AuthorizationRequestBasedOnCookieRepository.removeAuthorizationRequest(request, response);
    }
    /**
     * 토큰 정보를 쿼리 파라미터로 포함시킨 리디렉션 URI 생성
     */
    private String getTargetUrl(TokenResponseDto kakaoToken) {
        return UriComponentsBuilder.fromUriString(REDIRECT_URI)
            .queryParam("accessToken", kakaoToken.accessToken())
            .queryParam("refreshToken", kakaoToken.refreshToken())
            .build()
            .toUriString();
    }
}