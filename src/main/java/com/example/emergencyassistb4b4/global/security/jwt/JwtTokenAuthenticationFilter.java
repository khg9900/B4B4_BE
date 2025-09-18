package com.example.emergencyassistb4b4.global.security.jwt;

import com.example.emergencyassistb4b4.global.security.handler.JwtAuthenticationException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

        // 요청 헤더의 Authorization 키의 값 조회
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);

        //가져온 값에서 접두사 제거
        String token = getAccessToken(authorizationHeader);

        //가져온 토큰이 유효한지 확인하고 유효한 때는 인증 정보 설정
        try {
            if (token != null) {
                validateAndAuthenticateToken(token);
            }

            filterChain.doFilter(request, response);

        } catch (JwtAuthenticationException e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
            request.setAttribute("exception", e);
            throw e;
        }
    }

    // 유효성 검사 및 SecurityContext 설정 메서드 (기존 로직 그대로 분리)
    private void validateAndAuthenticateToken(String token) {

        if (Boolean.TRUE.equals(redisTemplate.hasKey(token))) {
            throw new JwtAuthenticationException(ErrorStatus.LOGOUT_TOKEN); // 새 ErrorStatus 필요
        }

        try {
            jwtUtils.validateToken(token);

            Authentication authentication = jwtUtils.getAuthentication(token);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException(ErrorStatus.EXPIRED_ACCESS_TOKEN);

        } catch (MalformedJwtException | UnsupportedJwtException e) {
            throw new JwtAuthenticationException(ErrorStatus.INVALID_ACCESS_TOKEN);

        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException(ErrorStatus.INVALID_ACCESS_TOKEN);

        } catch (Exception e) {
            throw new JwtAuthenticationException(ErrorStatus.CUSTOM_ERROR_STATUS);
        }
    }

    private String getAccessToken(String authorizationHeader) {

        if (authorizationHeader != null && authorizationHeader.startsWith(HEADER_PREFIX)) {
            return authorizationHeader.substring(HEADER_PREFIX.length());
        }

        return null;
    }
}