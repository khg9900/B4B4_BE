package com.example.emergencyassistb4b4.auth.oauth.repository;

import com.example.emergencyassistb4b4.global.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.WebUtils;
/**
 * OAuth2 인증 요청을 쿠키에 저장하고 관리하는 Repository 구현체.
 * OAuth2 인증 과정에서 Provider로 리다이렉트하기 전에 인증 요청 상태를 쿠키에 저장하여,
 * 콜백 시 이 상태를 복원하여 요청의 유효성을 검증하고 CSRF 공격을 방지합니다.
 */
@Repository
public class OAuth2AuthorizationRequestBasedOnCookieRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    public final static String COOKIE_NAME = "oauth2";
    private final static int COOKIE_EXPIRE_SECONDS = 300;
    /**
     * 요청(Request)에서 쿠키에 저장된 OAuth2 인증 요청 정보를 로드함
     *
     * @param request 현재 요청 객체
     * @return 쿠키에서 로드된 OAuth2AuthorizationRequest 객체 (없거나 오류 시 null)
     */
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);  // 요청에서 지정된 이름(COOKIE_NAME)의 쿠키를 가져옵니다.
        if (cookie == null) {
            return null;
        }
        // 가져온 쿠키가 있다면, CookieUtil을 사용하여 쿠키 값을 역직렬화하여
        // OAuth2AuthorizationRequest 객체로 변환하여 반환합니다.
        return (OAuth2AuthorizationRequest) CookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class);
    }
    /**
     * OAuth2 인증 요청 정보를 쿠키에 저장합니다.
     */
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if(authorizationRequest == null) {// 저장할 authorizationRequest가 null이면 기존 쿠키를 삭제합니다.
            removeAuthorizationRequest(request, response);
            return;
        }
        CookieUtil.addCookie(response,COOKIE_NAME,
                CookieUtil.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
        // authorizationRequest 객체를 CookieUtil을 사용하여 직렬화하고,
        // 지정된 쿠키 이름과 만료 시간으로 응답(Response)에 쿠키를 추가합니다.


    }
    /**
     * 요청에서 쿠키에 저장된 OAuth2 인증 요청 정보를 로드하고
     * 삭제된 객체를 반환합니다.
     * 현재 구현은 단순히 로드만 하고 쿠키를 실제로 삭제하지 않습니다.
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 객체를 로드

        Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
        if (cookie == null || cookie.getValue().isBlank()) {
            removeAuthorizationRequestCookies(request, response);
            return null;
        }
        try {
            OAuth2AuthorizationRequest authorizationRequest = (OAuth2AuthorizationRequest)
                    CookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class);
            removeAuthorizationRequestCookies(request, response);
            return authorizationRequest;
        } catch (Exception e) {

            removeAuthorizationRequestCookies(request, response);
            return null;
        }
    }
    /**
     * OAuth2 인증 요청 정보를 저장한 쿠키를 삭제합니다.
     * 이 메서드는 인증 과정이 완료되거나 실패했을 때 호출됩니다.
     */
    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, COOKIE_NAME);
    }
}
