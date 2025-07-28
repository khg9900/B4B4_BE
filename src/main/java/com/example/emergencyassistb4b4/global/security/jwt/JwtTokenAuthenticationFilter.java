package com.example.emergencyassistb4b4.global.security.jwt;

import com.example.emergencyassistb4b4.global.security.handler.JwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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

    public final static String HEADER_AUTHORIZATION = "Authorization";
    public final static String HEADER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.debug("Request Path: {}", path); // 경로 로그 출력

        // 요청 헤더의 Authorization 키의 값 조회
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        String token = getAccessToken(authorizationHeader); //가져온 값에서 접두사 제거
        log.debug("Extracted Token: {}", token); // 추출한 토큰 로그

        //가져온 토큰이 유효한지 확인하고 유효한 때는 인증 정보 설정
        try {
            if (token != null) {
                validateAndAuthenticateToken(token); // 추출된 메서드로 인증 로직 분리
            } else {
                log.debug("No token found in request.");
            }

            filterChain.doFilter(request, response);

        } catch (JwtAuthenticationException e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
            // request에 예외를 저장하면 AuthenticationEntryPoint에서 꺼내어 응답을 만들 수 있음
            request.setAttribute("exception", e);
            throw e;
        }
    }

    // 유효성 검사 및 SecurityContext 설정 메서드 (기존 로직 그대로 분리)
    private void validateAndAuthenticateToken(String token) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(token))) {
            log.warn("Token is blacklisted (logged out).");
            throw new JwtAuthenticationException("로그아웃 된 토큰입니다.");
        }

        if (!jwtUtils.validateToken(token)) {
            log.warn("Invalid JWT token.");
            throw new JwtAuthenticationException("유효하지 않은 JWT 토큰입니다.");
        }

        Authentication authentication = jwtUtils.getAuthentication(token);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        log.debug("Authentication successful, user: {}", authentication.getName());
    }

    private String getAccessToken(String authorizationHeader) {

        log.debug("Authorization Header: {}", authorizationHeader);

        if (authorizationHeader != null && authorizationHeader.startsWith(HEADER_PREFIX)) {
            return authorizationHeader.substring(HEADER_PREFIX.length());
        }

        return null;
    }
}