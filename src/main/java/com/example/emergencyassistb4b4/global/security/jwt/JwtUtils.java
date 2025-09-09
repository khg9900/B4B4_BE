package com.example.emergencyassistb4b4.global.security.jwt;

import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.security.auth.CustomUserDetails;
import com.example.emergencyassistb4b4.global.security.config.JwtProperties;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
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
import io.jsonwebtoken.security.SignatureException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

// 1. 토큰 생성 generateAccessToken(), generateRefreshToken(), createToken()
// 2. 인증 객체 반환 getAuthentication()
// 3. 유효성 검증 validateToken()
// 4. 클레임 조회 getClaims(), getUserId()
@RequiredArgsConstructor
@Component
@Slf4j
public class JwtUtils {

    private final JwtProperties jwtProperties; //jwt 비밀키 등의 값을 주입받기 위한 객체
    private final UserRepository userRepository;

    /**
     * Access Token 생성, 1시간 유효
     */
    public String generateAccessToken(UserResponseDto userResponseDto) {

        return createToken(userResponseDto, Duration.ofSeconds(jwtProperties.getAccessTokenValidity()));
    }

    /**
     * 사용자 정보를 기반으로 Refresh 토큰 생성 및 Redis 에 저장
     * @param userResponseDto 토큰에 포함될 사용자 정보
     * @return  JWT 문자열
     */
    public String generateRefreshToken(UserResponseDto userResponseDto) {

        return createToken(userResponseDto, Duration.ofSeconds(jwtProperties.getRefreshTokenValidity()));
    }

    /**
     * 만료 시간과 사용자 정보를 기반으로 JWT를 생성
     * @param duration 토큰 만료 시각
     * @param user 사용자 정보
     * @return JWT 문자열
     */
    public String createToken(UserResponseDto user, Duration duration) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + duration.toMillis());

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // 헤더 typ : JWT
                .setIssuer(jwtProperties.getIssuer()) // 내용 iss : yml 파일에서 설정한 값
                .setIssuedAt(now) // 내용 iat : 현재 시간
                .setExpiration(expiry) // 내용 exp : expiry 멤버 변숫값
                .setSubject(user.getEmail()) // 내용 sub : 유저의 이메일
                .claim("id", user.getId()) // 클레임 id : 유저 ID
                .claim("role", user.getUserRole().name())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                // 서명 : 비밀값과 함께 해시값을 ~ 방식으로 암호화
                .compact();
    }

    private Key getSigningKey() {

        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 토큰 유효성 검증 메서드
     * yml 파일에 선언한 비밀값과 함께 토큰 복호화를 진행 후 아무 에러도 발생하지 않으면 true 반환
     * @param  token 토큰
     * @return boolean 값
     */
    // 1. 단순 boolean 반환용
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

    // 2. 예외 던지는 검증용
    public void validateTokenOrThrow(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new ApiException(ErrorStatus.EXPIRED_ACCESS_TOKEN);
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new ApiException(ErrorStatus.INVALID_ACCESS_TOKEN);
        } catch (Exception e) {
            throw new ApiException(ErrorStatus.CUSTOM_ERROR_STATUS);
        }
    }

    /**
     * 토큰 기반으로 인증 정보를 가져오는 메서드
     * @param token
     * @return Authentication 토큰을 받아 인증 정보를 담은 객체 Authentication 을 반환
     */
    public Authentication getAuthentication(String token) {

        Claims claims = getClaims(token); // jwt에서 claims(사용자 정보 등)를 추출
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        Set<SimpleGrantedAuthority> authorities =
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role));

        // 이메일 기반으로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));


        CustomUserDetails userDetails = new CustomUserDetails(user);

        return new UsernamePasswordAuthenticationToken(
                userDetails, // principal
                token,       // credentials (보통은 null 또는 token)
                userDetails.getAuthorities() // 권한
        );
    }

    public Long getUserId(String token) { // JWT에서 사용자 ID를 추출하는 메서드

        Claims claims = getClaims(token); // 클레임 정보 추출

        return claims.get("id", Long.class); // "id" 키를 Long 타입으로 가져옴
    }

    // JWT에서 Claims(페이로드 부분)를 파싱해 가져오는 내부 유틸 메서드
    private Claims getClaims(String token) {

        return Jwts.parserBuilder() //  JWT 파서 생성
                .setSigningKey(getSigningKey()) // 시크릿 키 설정
                .build()
                .parseClaimsJws(token) // JWT 문자열을 파싱
                .getBody(); // 클레임(body) 반환
    }

    public String getEmailFromToken(String token) {

        return getClaims(token).getSubject();
    }

    public long getRemainingExpiration(String token) {

        Date expiration = getClaims(token).getExpiration();

        return expiration.getTime() - System.currentTimeMillis();
    }

    public String resolveToken(ServletRequest request) {

        HttpServletRequest req = (HttpServletRequest) request;
        String bearerToken = req.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
