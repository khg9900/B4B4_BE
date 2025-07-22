package com.example.emergencyassistb4b4.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

// 1. /api/auth, /oauth2 등 경로는 필터 제외
// 2. Authorization 헤더의 Bearer 토큰 추출
// 3. JWT 유효성 검증 → 성공 시 SecurityContext에 Authentication 설정
@RequiredArgsConstructor
@Component
@Slf4j

public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    public final static String HEADER_AUTHORIZATION = "Authorization";
    public final static String HEADER_PREFIX = "Bearer ";
    private final PathMatcher pathMatcher;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.debug("Request Path: {}", path); // 경로 로그 출력
        // 필터 예외 경로 처리
        if (isSkipPath(path)) {
            log.debug("Skipping path: {}", path); // 필터를 건너뛴 경로 로그
            filterChain.doFilter(request, response);
            return;
        }

        // 요청 헤더의 Authorization 키의 값 조회
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        log.debug("Authorization Header: {}", authorizationHeader); // 헤더 출력

        //가져온 값에서 접두사 제거
        String token = getAccessToken(authorizationHeader);
        log.debug("Extracted Token: {}", token); // 추출한 토큰 로그

        //가져온 토큰이 유효한지 확인하고 유효한 때는 인증 정보 설정
        if (token != null) {
            if (jwtUtils.validateToken(token)) {
                Authentication authentication = jwtUtils.getAuthentication(token);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
                log.debug("Authentication successful, user: {}", authentication.getName()); // 인증 성공 로그


            } else {
                log.warn("Invalid JWT token.");
            }

        } else {
            log.debug("No token found in request.");
        }

        filterChain.doFilter(request, response);
    }

    private String getAccessToken(String authorizationHeader) {

        if (authorizationHeader != null) {
            log.debug("Authorization Header: {}", authorizationHeader);  // 헤더 값 확인
            if (authorizationHeader.startsWith(HEADER_PREFIX)) {
                return authorizationHeader.substring(HEADER_PREFIX.length());
            }
        }

        return null;
    }

    // 필터 skip 경로 (로그인, 회원가입 등만 포함)
    private static final Set<String> SKIP_PATH = Set.of(
            "/api/auth/login",
            "/api/auth/signup",
            "/oauth2/**",
            "/api/login/oauth2/code/**"
    );

    private boolean isSkipPath(String path) {

        return SKIP_PATH.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}