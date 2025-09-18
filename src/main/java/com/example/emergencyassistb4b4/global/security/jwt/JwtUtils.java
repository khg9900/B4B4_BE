package com.example.emergencyassistb4b4.global.security.jwt;

import com.example.emergencyassistb4b4.global.security.auth.CustomUserDetails;
import com.example.emergencyassistb4b4.global.security.config.JwtProperties;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.dto.UserResponseDto;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtUtils {

    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    // Access Token 생성 (1시간 유효)
    public String generateAccessToken(UserResponseDto userResponseDto) {

        return createToken(userResponseDto, Duration.ofSeconds(jwtProperties.getAccessTokenValidity()));
    }

    // Refresh Token 생성
    public String generateRefreshToken(UserResponseDto userResponseDto) {

        return createToken(userResponseDto, Duration.ofSeconds(jwtProperties.getRefreshTokenValidity()));
    }

    // JWT 생성 (만료 시간, 사용자 정보 포함)
    public String createToken(UserResponseDto user, Duration duration) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + duration.toMillis());

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // 헤더 typ : JWT
                .setIssuer(jwtProperties.getIssuer()) // 내용 iss : yml 파일에서 설정한 값
                .setIssuedAt(now) // 현재 시간
                .setExpiration(expiry) // expiry 멤버 변숫값
                .setSubject(user.getEmail()) // 유저의 이메일
                .claim("id", user.getId()) // 유저 ID
                .claim("role", user.getUserRole().name())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 비밀값과 함께 해시값을 ~ 방식으로 암호화
                .compact();
    }

    // 서명 키 생성
    private Key getSigningKey() {

        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰 기반 인증 객체 생성
    public Authentication getAuthentication(String token) {

        Claims claims = getClaims(token);
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        Set<SimpleGrantedAuthority> authorities =
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));


        CustomUserDetails userDetails = new CustomUserDetails(user);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                token,
                userDetails.getAuthorities()
        );
    }

    // 토큰에서 사용자 ID 추출
    public Long getUserId(String token) {

        Claims claims = getClaims(token);

        return claims.get("id", Long.class);
    }

    // 토큰에서 Claims 추출
    private Claims getClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰에서 이메일 추출
    public String getEmailFromToken(String token) {

        return getClaims(token).getSubject();
    }

    // 토큰 만료까지 남은 시간(ms)
    public long getRemainingExpiration(String token) {

        Date expiration = getClaims(token).getExpiration();

        return expiration.getTime() - System.currentTimeMillis();
    }

    // HttpServletRequest에서 Bearer 토큰 추출
    public String resolveToken(ServletRequest request) {

        HttpServletRequest req = (HttpServletRequest) request;
        String bearerToken = req.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
